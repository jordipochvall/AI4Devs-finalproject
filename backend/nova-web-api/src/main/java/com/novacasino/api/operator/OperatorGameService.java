package com.novacasino.api.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.novacasino.api.operator.dto.OperatorGameDto;
import com.novacasino.api.operator.dto.UpdateGameRequest;
import com.novacasino.api.operator.exception.InvalidCommercialConfigException;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameCommercialAuditEntity;
import com.novacasino.infrastructure.persistence.entity.GameCommercialEntity;
import com.novacasino.infrastructure.persistence.repository.GameCommercialAuditJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameCommercialJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Operator use cases for a game's <b>commercial</b> configuration (HU-15): listing and updating bet
 * bounds, currencies and the active flag (never the math). Every update is recorded append-only in
 * {@code game_commercial_audits} with a before/after snapshot, author and timestamp (decision D5).
 */
@Service
public class OperatorGameService {

    private final GameCommercialJpaRepository gamesRepo;
    private final GameCommercialAuditJpaRepository auditRepo;
    private final GameConfigJpaRepository configRepo;
    private final ObjectMapper objectMapper;

    public OperatorGameService(final GameCommercialJpaRepository gamesRepo,
                               final GameCommercialAuditJpaRepository auditRepo,
                               final GameConfigJpaRepository configRepo,
                               final ObjectMapper objectMapper) {
        this.gamesRepo    = gamesRepo;
        this.auditRepo    = auditRepo;
        this.configRepo   = configRepo;
        this.objectMapper = objectMapper;
    }

    /** Lists the operator's games with their commercial configuration. */
    @Transactional(readOnly = true)
    public List<OperatorGameDto> listGames(final Long operatorId) {
        return gamesRepo.findByOperatorIdOrderByIdAsc(operatorId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Updates a game's commercial configuration and records the change in the audit log.
     *
     * @throws GameNotFoundException             if the game does not exist within the operator (AC5)
     * @throws InvalidCommercialConfigException  on incoherent bounds or non-payline-multiple bet/step
     */
    @Transactional
    public OperatorGameDto updateGame(final Long operatorId, final Long gameId,
                                      final Long performedByUserId, final UpdateGameRequest req) {
        final GameCommercialEntity game = gamesRepo.findByIdAndOperatorId(gameId, operatorId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        validate(req, paylineCount(game.getActiveConfigId()));

        final String before = snapshot(game);

        game.setMinBetCents(req.minBetCents());
        game.setMaxBetCents(req.maxBetCents());
        game.setBetStepCents(req.betStepCents());
        game.setActive(req.active());
        if (req.allowedCurrencies() != null && !req.allowedCurrencies().isEmpty()) {
            game.setAllowedCurrencies(req.allowedCurrencies().toArray(new String[0]));
        }
        game.setUpdatedAt(OffsetDateTime.now());
        gamesRepo.save(game);

        final String after = snapshot(game);
        auditRepo.save(new GameCommercialAuditEntity(operatorId, gameId, performedByUserId, before, after));

        return toDto(game);
    }

    // -------------------------------------------------------------------------

    /** Coherence checks (AC3): ordered positive bounds and bet/step multiples of the payline count. */
    private void validate(final UpdateGameRequest req, final Integer paylineCount) {
        if (req.maxBetCents() < req.minBetCents()) {
            throw new InvalidCommercialConfigException("maxBetCents must be >= minBetCents");
        }
        if (paylineCount != null && paylineCount > 0) {
            if (req.minBetCents() % paylineCount != 0) {
                throw new InvalidCommercialConfigException("minBetCents must be a multiple of the payline count");
            }
            if (req.betStepCents() % paylineCount != 0) {
                throw new InvalidCommercialConfigException("betStepCents must be a multiple of the payline count");
            }
        }
    }

    /** Payline count of the active math config (null when there is none). */
    private Integer paylineCount(final Long activeConfigId) {
        if (activeConfigId == null) {
            return null;
        }
        return configRepo.findById(activeConfigId)
                .map(c -> parse(c.getConfig()).get("paylines").size())
                .orElse(null);
    }

    /** Compact JSON snapshot of the commercial fields, for the audit before/after. */
    private String snapshot(final GameCommercialEntity game) {
        final ObjectNode node = objectMapper.createObjectNode();
        node.put("minBetCents", game.getMinBetCents());
        node.put("maxBetCents", game.getMaxBetCents());
        node.put("betStepCents", game.getBetStepCents());
        node.put("active", game.isActive());
        node.set("allowedCurrencies", objectMapper.valueToTree(currencies(game)));
        return toJson(node);
    }

    private OperatorGameDto toDto(final GameCommercialEntity game) {
        return new OperatorGameDto(game.getId(), game.getCode(), game.getName(), game.getTheme(),
                game.getMinBetCents(), game.getMaxBetCents(), game.getBetStepCents(),
                currencies(game), game.isActive(), game.getActiveConfigId(),
                paylineCount(game.getActiveConfigId()));
    }

    /** Currencies as a trimmed list (the column is {@code char(3)} so values are not padded here). */
    private List<String> currencies(final GameCommercialEntity game) {
        if (game.getAllowedCurrencies() == null) {
            return List.of();
        }
        return Arrays.stream(game.getAllowedCurrencies()).map(String::trim).toList();
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final Exception e) {
            throw new IllegalStateException("Invalid config JSON in database", e);
        }
    }

    private String toJson(final JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize the commercial snapshot", e);
        }
    }
}
