package com.novacasino.api.math;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.math.dto.ConfigCreatedDto;
import com.novacasino.api.math.dto.ConfigDetailDto;
import com.novacasino.api.math.dto.ConfigVersionDto;
import com.novacasino.api.math.dto.CreateConfigRequest;
import com.novacasino.api.math.dto.MathGameDto;
import com.novacasino.api.math.dto.PublishResultDto;
import com.novacasino.api.math.exception.ConfigAlreadyActiveException;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.math.validation.ConfigValidationException;
import com.novacasino.api.math.validation.ConfigValidator;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameConfigPublicationEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameConfigPublicationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    private final GameConfigPublicationJpaRepository publicationRepo;
    private final ConfigValidator validator;
    private final ObjectMapper objectMapper;

    public MathService(final GameJpaRepository gameRepo, final GameConfigJpaRepository configRepo,
                       final GameConfigPublicationJpaRepository publicationRepo,
                       final ConfigValidator validator, final ObjectMapper objectMapper) {
        this.gameRepo        = gameRepo;
        this.configRepo      = configRepo;
        this.publicationRepo = publicationRepo;
        this.validator       = validator;
        this.objectMapper    = objectMapper;
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

    /**
     * Lists a game's math versions (newest first), flagging which one is currently active (HU-17).
     * The game must exist and belong to the operator.
     */
    @Transactional(readOnly = true)
    public List<ConfigVersionDto> listConfigs(final Long gameId, final Long operatorId) {
        final GameEntity game = ownedGame(gameId, operatorId);
        final Long activeId = game.getActiveConfigId();
        return configRepo.findByGameIdOrderByVersionDesc(gameId).stream()
                .map(c -> new ConfigVersionDto(c.getId(), c.getVersion(), c.getRtpTarget(),
                        c.getVolatilityTarget(), c.getId().equals(activeId), c.getCreatedAt()))
                .toList();
    }

    /**
     * Publishes (activates) a math version (HU-17): moves {@code games.active_config_id} to the chosen
     * version and records an immutable publication. New player spins then use this version.
     *
     * @throws ConfigAlreadyActiveException if the version is already the active one (409)
     * @throws ConfigNotFoundException      if the version does not exist or does not belong to the game
     */
    @Transactional
    public PublishResultDto publish(final Long gameId, final Long configId,
                                    final Long operatorId, final Long mathUserId) {
        final GameEntity game = ownedGame(gameId, operatorId);

        final GameConfigEntity config = configRepo.findById(configId)
                .filter(c -> c.getGameId().equals(gameId))
                .orElseThrow(() -> new ConfigNotFoundException(configId));

        if (configId.equals(game.getActiveConfigId())) {
            throw new ConfigAlreadyActiveException(configId);
        }

        // Move the active pointer and record the (append-only) publication.
        game.setActiveConfigId(config.getId());
        game.setUpdatedAt(OffsetDateTime.now());
        gameRepo.save(game);

        final GameConfigPublicationEntity publication = publicationRepo.save(
                new GameConfigPublicationEntity(operatorId, gameId, config.getId(), mathUserId));

        return new PublishResultDto(gameId, config.getId(), config.getVersion(),
                mathUserId, publication.getPublishedAt());
    }

    /** Creates a new math version (does not publish it). */
    @Transactional
    public ConfigCreatedDto createConfig(final Long gameId, final Long operatorId,
                                         final Long mathUserId, final CreateConfigRequest req) {
        // The game must exist and belong to the operator
        ownedGame(gameId, operatorId);

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

    /** Loads a game ensuring it exists and belongs to the operator (else 404). */
    private GameEntity ownedGame(final Long gameId, final Long operatorId) {
        return gameRepo.findById(gameId)
                .filter(g -> g.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

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
