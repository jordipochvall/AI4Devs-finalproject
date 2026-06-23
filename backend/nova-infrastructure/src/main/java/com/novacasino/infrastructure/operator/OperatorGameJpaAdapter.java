package com.novacasino.infrastructure.operator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.application.operator.GameCommercialUpdate;
import com.novacasino.application.operator.OperatorGamePort;
import com.novacasino.common.dto.OperatorGameDto;
import com.novacasino.infrastructure.persistence.entity.GameCommercialAuditEntity;
import com.novacasino.infrastructure.persistence.entity.GameCommercialEntity;
import com.novacasino.infrastructure.persistence.repository.GameCommercialAuditJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameCommercialJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter for {@link OperatorGamePort} (HU-15): commercial-config read/update with an append-only
 * before/after JSON snapshot in {@code game_commercial_audits}. JSON handling lives here, not in the
 * use case.
 */
@Component
public class OperatorGameJpaAdapter implements OperatorGamePort {

    private final GameCommercialJpaRepository gamesRepo;
    private final GameCommercialAuditJpaRepository auditRepo;
    private final GameConfigJpaRepository configRepo;
    private final ObjectMapper objectMapper;

    public OperatorGameJpaAdapter(final GameCommercialJpaRepository gamesRepo,
                                  final GameCommercialAuditJpaRepository auditRepo,
                                  final GameConfigJpaRepository configRepo, final ObjectMapper objectMapper) {
        this.gamesRepo = gamesRepo;
        this.auditRepo = auditRepo;
        this.configRepo = configRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<OperatorGameDto> listGames(final Long operatorId) {
        return gamesRepo.findByOperatorIdOrderByIdAsc(operatorId).stream().map(this::toDto).toList();
    }

    @Override
    public Optional<Integer> findGamePaylineCount(final Long operatorId, final Long gameId) {
        // empty => game not in operator; 0 => exists but no active config; n => payline count.
        return gamesRepo.findByIdAndOperatorId(gameId, operatorId)
                .map(g -> {
                    final Integer pc = paylineCount(g.getActiveConfigId());
                    return pc == null ? 0 : pc;
                });
    }

    @Override
    public OperatorGameDto applyUpdate(final Long operatorId, final Long gameId,
                                       final Long performedByUserId, final GameCommercialUpdate update) {
        final GameCommercialEntity game = gamesRepo.findByIdAndOperatorId(gameId, operatorId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        final String before = snapshot(game);
        game.setMinBetCents(update.minBetCents());
        game.setMaxBetCents(update.maxBetCents());
        game.setBetStepCents(update.betStepCents());
        game.setActive(update.active());
        if (update.allowedCurrencies() != null && !update.allowedCurrencies().isEmpty()) {
            game.setAllowedCurrencies(update.allowedCurrencies().toArray(new String[0]));
        }
        game.setUpdatedAt(OffsetDateTime.now());
        gamesRepo.save(game);

        final String after = snapshot(game);
        auditRepo.save(new GameCommercialAuditEntity(operatorId, gameId, performedByUserId, before, after));
        return toDto(game);
    }

    // -------------------------------------------------------------------------

    private Integer paylineCount(final Long activeConfigId) {
        if (activeConfigId == null) {
            return null;
        }
        return configRepo.findById(activeConfigId)
                .map(c -> parse(c.getConfig()).get("paylines").size()).orElse(null);
    }

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
