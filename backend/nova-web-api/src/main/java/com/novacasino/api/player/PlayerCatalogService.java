package com.novacasino.api.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.player.dto.GameDetailDto;
import com.novacasino.api.player.dto.GameSummaryDto;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Player read operations: lobby catalogue, game detail (+ active config) and balance.
 */
@Service
@Transactional(readOnly = true)
public class PlayerCatalogService {

    private final GameJpaRepository gameRepo;
    private final GameConfigJpaRepository configRepo;
    private final WalletJpaRepository walletRepo;
    private final ObjectMapper objectMapper;

    public PlayerCatalogService(final GameJpaRepository gameRepo,
                                final GameConfigJpaRepository configRepo,
                                final WalletJpaRepository walletRepo,
                                final ObjectMapper objectMapper) {
        this.gameRepo     = gameRepo;
        this.configRepo   = configRepo;
        this.walletRepo   = walletRepo;
        this.objectMapper = objectMapper;
    }

    /** Lobby catalogue: active games, exposing only `grid` (not the full math). */
    public List<GameSummaryDto> listActiveGames() {
        return gameRepo.findByActiveTrueOrderByIdAsc().stream()
                .map(game -> new GameSummaryDto(
                        game.getId(),
                        game.getName(),
                        game.getTheme(),
                        game.getCoverImageUrl(),
                        gridOf(game)))
                .toList();
    }

    /** Active game detail plus its active config. 404 if it does not exist or is inactive. */
    public GameDetailDto getActiveGame(final Long gameId) {
        final GameEntity game = gameRepo.findByIdAndActiveTrue(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        final JsonNode config = activeConfigNode(game);
        return new GameDetailDto(
                game.getId(),
                game.getName(),
                game.getTheme(),
                game.getCoverImageUrl(),
                game.getMinBetCents(),
                game.getMaxBetCents(),
                game.getBetStepCents(),
                config);
    }

    /** Balance of the authenticated player. */
    public WalletDto getWallet(final Long userId) {
        return walletRepo.findByUserId(userId)
                .map(w -> new WalletDto(w.getBalanceCents(), w.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
    }

    // -------------------------------------------------------------------------

    /** Full active config as a JSON tree. */
    private JsonNode activeConfigNode(final GameEntity game) {
        if (game.getActiveConfigId() == null) {
            // Without an active config the game is not playable → treat as unavailable.
            throw new GameNotFoundException(game.getId());
        }
        final GameConfigEntity cfg = configRepo.findById(game.getActiveConfigId())
                .orElseThrow(() -> new GameNotFoundException(game.getId()));
        return parse(cfg.getConfig());
    }

    /** Only the `grid` node of the active config (for the lobby); null if there is none. */
    private JsonNode gridOf(final GameEntity game) {
        if (game.getActiveConfigId() == null) {
            return null;
        }
        return configRepo.findById(game.getActiveConfigId())
                .map(cfg -> parse(cfg.getConfig()).get("grid"))
                .orElse(null);
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException("Invalid config JSON in database", e);
        }
    }
}
