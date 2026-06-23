package com.novacasino.infrastructure.math;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.application.math.CreateConfigCommand;
import com.novacasino.application.math.MathConfigPort;
import com.novacasino.common.dto.ConfigCreatedDto;
import com.novacasino.common.dto.ConfigDetailDto;
import com.novacasino.common.dto.ConfigVersionDto;
import com.novacasino.common.dto.MathGameDto;
import com.novacasino.common.dto.PublishResultDto;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameConfigPublicationEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameConfigPublicationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** JPA adapter for {@link MathConfigPort} (HU-17): version read, publish and immutable create. */
@Component
public class MathConfigJpaAdapter implements MathConfigPort {

    private final GameJpaRepository gameRepo;
    private final GameConfigJpaRepository configRepo;
    private final GameConfigPublicationJpaRepository publicationRepo;
    private final ObjectMapper objectMapper;

    public MathConfigJpaAdapter(final GameJpaRepository gameRepo, final GameConfigJpaRepository configRepo,
                                final GameConfigPublicationJpaRepository publicationRepo,
                                final ObjectMapper objectMapper) {
        this.gameRepo = gameRepo;
        this.configRepo = configRepo;
        this.publicationRepo = publicationRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<MathGameDto> listGames(final Long operatorId) {
        return gameRepo.findByOperatorIdOrderByIdAsc(operatorId).stream()
                .map(g -> new MathGameDto(g.getId(), g.getCode(), g.getName(), g.getTheme(),
                        g.isActive(), g.getActiveConfigId(), activeVersion(g.getActiveConfigId())))
                .toList();
    }

    @Override
    public Optional<ConfigDetailDto> getConfig(final Long configId) {
        return configRepo.findById(configId).map(cfg -> new ConfigDetailDto(
                cfg.getId(), cfg.getGameId(), cfg.getVersion(),
                cfg.getRtpTarget(), cfg.getVolatilityTarget(), cfg.getNotes(), parse(cfg.getConfig())));
    }

    @Override
    public Optional<OwnedGame> findOwnedGame(final Long gameId, final Long operatorId) {
        return gameRepo.findById(gameId)
                .filter(g -> g.getOperatorId().equals(operatorId))
                .map(g -> new OwnedGame(g.getActiveConfigId()));
    }

    @Override
    public List<ConfigVersionDto> listConfigs(final Long gameId, final Long activeConfigId) {
        return configRepo.findByGameIdOrderByVersionDesc(gameId).stream()
                .map(c -> new ConfigVersionDto(c.getId(), c.getVersion(), c.getRtpTarget(),
                        c.getVolatilityTarget(), c.getId().equals(activeConfigId), c.getCreatedAt()))
                .toList();
    }

    @Override
    public Optional<ConfigRef> findConfig(final Long configId) {
        return configRepo.findById(configId)
                .map(c -> new ConfigRef(c.getId(), c.getGameId(), c.getVersion()));
    }

    @Override
    public PublishResultDto applyPublish(final Long gameId, final Long configId,
                                         final Long operatorId, final Long mathUserId) {
        final GameEntity game = gameRepo.findById(gameId)
                .orElseThrow(() -> new IllegalStateException("Game not found: " + gameId));
        final GameConfigEntity config = configRepo.findById(configId)
                .orElseThrow(() -> new IllegalStateException("Config not found: " + configId));

        game.setActiveConfigId(config.getId());
        game.setUpdatedAt(OffsetDateTime.now());
        gameRepo.save(game);

        final GameConfigPublicationEntity publication = publicationRepo.save(
                new GameConfigPublicationEntity(operatorId, gameId, config.getId(), mathUserId));

        return new PublishResultDto(gameId, config.getId(), config.getVersion(),
                mathUserId, publication.getPublishedAt());
    }

    @Override
    public ConfigCreatedDto createConfig(final Long gameId, final Long mathUserId, final CreateConfigCommand cmd) {
        final int nextVersion = configRepo.findTopByGameIdOrderByVersionDesc(gameId)
                .map(c -> c.getVersion() + 1)
                .orElse(1);
        final GameConfigEntity saved = configRepo.save(new GameConfigEntity(
                gameId, nextVersion, toJson(cmd.config()), cmd.rtpTarget(),
                cmd.volatilityTarget(), mathUserId, cmd.notes()));
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
