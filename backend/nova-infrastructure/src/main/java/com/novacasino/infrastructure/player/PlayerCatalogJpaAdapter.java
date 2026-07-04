package com.novacasino.infrastructure.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.application.player.PlayerCatalogPort;
import com.novacasino.common.dto.GameDetailDto;
import com.novacasino.common.dto.GameSummaryDto;
import com.novacasino.common.dto.WalletDto;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.JackpotPoolJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** JPA adapter for {@link PlayerCatalogPort} (HU-5): catalogue, detail (+config/jackpot) and wallet. */
@Component
public class PlayerCatalogJpaAdapter implements PlayerCatalogPort {

    private final GameJpaRepository gameRepo;
    private final GameConfigJpaRepository configRepo;
    private final WalletJpaRepository walletRepo;
    private final JackpotPoolJpaRepository jackpotPoolRepo;
    private final ObjectMapper objectMapper;

    public PlayerCatalogJpaAdapter(final GameJpaRepository gameRepo, final GameConfigJpaRepository configRepo,
                                   final WalletJpaRepository walletRepo,
                                   final JackpotPoolJpaRepository jackpotPoolRepo, final ObjectMapper objectMapper) {
        this.gameRepo = gameRepo;
        this.configRepo = configRepo;
        this.walletRepo = walletRepo;
        this.jackpotPoolRepo = jackpotPoolRepo;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSummaryDto> listActiveGames() {
        return gameRepo.findByActiveTrueOrderByIdAsc().stream()
                .map(g -> new GameSummaryDto(g.getId(), g.getName(), g.getTheme(),
                        g.getCoverImageUrl(), gridOf(g)))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GameDetailDto> activeGame(final Long gameId) {
        return gameRepo.findByIdAndActiveTrue(gameId).flatMap(game -> {
            if (game.getActiveConfigId() == null) {
                return Optional.empty(); // not playable → treat as not found
            }
            return configRepo.findById(game.getActiveConfigId()).map(cfg -> {
                final Long jackpotCents = jackpotPoolRepo.findByGameId(game.getId())
                        .map(p -> p.getCurrentCents()).orElse(null);
                return new GameDetailDto(game.getId(), game.getName(), game.getTheme(),
                        game.getCoverImageUrl(), game.getMinBetCents(), game.getMaxBetCents(),
                        game.getBetStepCents(), parse(cfg.getConfig()), jackpotCents);
            });
        });
    }

    @Override
    public Optional<WalletDto> walletOf(final Long userId) {
        return walletRepo.findByUserId(userId).map(w -> new WalletDto(w.getBalanceCents(), w.getCurrency()));
    }

    private JsonNode gridOf(final GameEntity game) {
        if (game.getActiveConfigId() == null) {
            return null;
        }
        return configRepo.findById(game.getActiveConfigId())
                .map(cfg -> parse(cfg.getConfig()).get("grid")).orElse(null);
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final Exception e) {
            throw new IllegalStateException("Invalid config JSON in database", e);
        }
    }
}
