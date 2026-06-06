package com.novacasino.api.math;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.math.dto.ConfigCreatedDto;
import com.novacasino.api.math.dto.ConfigDetailDto;
import com.novacasino.api.math.dto.CreateConfigRequest;
import com.novacasino.api.math.dto.MathGameDto;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.math.validation.ConfigValidationException;
import com.novacasino.api.math.validation.ConfigValidator;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Math backoffice use cases: querying games/versions and creating a new immutable math version
 * (with invariant validation, §3.3.3). Publishing (activating a version) is post-MVP: creating
 * does NOT change {@code games.active_config_id}.
 */
@Service
public class MathService {

    private final GameJpaRepository gameRepo;
    private final GameConfigJpaRepository configRepo;
    private final ConfigValidator validator;
    private final ObjectMapper objectMapper;

    public MathService(final GameJpaRepository gameRepo, final GameConfigJpaRepository configRepo,
                       final ConfigValidator validator, final ObjectMapper objectMapper) {
        this.gameRepo     = gameRepo;
        this.configRepo   = configRepo;
        this.validator    = validator;
        this.objectMapper = objectMapper;
    }

    /** Lists the operator's games with their active math version. */
    @Transactional(readOnly = true)
    public List<MathGameDto> listGames(final Long operatorId) {
        return gameRepo.findByOperatorIdOrderByIdAsc(operatorId).stream()
                .map(g -> new MathGameDto(
                        g.getId(), g.getCode(), g.getName(), g.getTheme(), g.isActive(),
                        g.getActiveConfigId(), activeVersion(g.getActiveConfigId())))
                .toList();
    }

    /** Returns the detail of a config version. */
    @Transactional(readOnly = true)
    public ConfigDetailDto getConfig(final Long configId) {
        final GameConfigEntity cfg = configRepo.findById(configId)
                .orElseThrow(() -> new ConfigNotFoundException(configId));
        return new ConfigDetailDto(
                cfg.getId(), cfg.getGameId(), cfg.getVersion(),
                cfg.getRtpTarget(), cfg.getVolatilityTarget(), cfg.getNotes(),
                parse(cfg.getConfig()));
    }

    /** Creates a new math version (does not publish it). */
    @Transactional
    public ConfigCreatedDto createConfig(final Long gameId, final Long operatorId,
                                         final Long mathUserId, final CreateConfigRequest req) {
        // The game must exist and belong to the operator
        gameRepo.findById(gameId)
                .filter(g -> g.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new GameNotFoundException(gameId));

        // Config invariants (§3.3.3)
        validator.validate(req.config());

        // Declared target RTP: must be within [0,1]
        final BigDecimal rtp = req.rtpTarget();
        if (rtp == null || rtp.signum() < 0 || rtp.compareTo(BigDecimal.ONE) > 0) {
            throw new ConfigValidationException(List.of(
                    new ConfigValidationException.FieldError("rtpTarget", "must be between 0 and 1")));
        }

        final int nextVersion = configRepo.findTopByGameIdOrderByVersionDesc(gameId)
                .map(c -> c.getVersion() + 1)
                .orElse(1);

        final GameConfigEntity saved = configRepo.save(new GameConfigEntity(
                gameId, nextVersion, toJson(req.config()), rtp,
                req.volatilityTarget(), mathUserId, req.notes()));

        return new ConfigCreatedDto(saved.getId(), gameId, saved.getVersion(),
                saved.getRtpTarget(), saved.getVolatilityTarget());
    }

    // -------------------------------------------------------------------------

    private Integer activeVersion(final Long activeConfigId) {
        if (activeConfigId == null) {
            return null;
        }
        return configRepo.findById(activeConfigId).map(GameConfigEntity::getVersion).orElse(null);
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
            throw new IllegalStateException("Could not serialize the config", e);
        }
    }
}
