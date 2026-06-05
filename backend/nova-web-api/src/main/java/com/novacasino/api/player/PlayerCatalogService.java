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
 * Lecturas del jugador: catálogo del lobby, detalle de juego (+ config activa) y saldo.
 */
@Service
@Transactional(readOnly = true)
public class PlayerCatalogService {

    private final GameJpaRepository gameRepo;
    private final GameConfigJpaRepository configRepo;
    private final WalletJpaRepository walletRepo;
    private final ObjectMapper objectMapper;

    public PlayerCatalogService(GameJpaRepository gameRepo,
                                GameConfigJpaRepository configRepo,
                                WalletJpaRepository walletRepo,
                                ObjectMapper objectMapper) {
        this.gameRepo     = gameRepo;
        this.configRepo   = configRepo;
        this.walletRepo   = walletRepo;
        this.objectMapper = objectMapper;
    }

    /** Catálogo del lobby: juegos activos, sin exponer la matemática (solo `grid`). */
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

    /** Detalle de un juego activo + su `config` activa. 404 si no existe/está inactivo. */
    public GameDetailDto getActiveGame(Long gameId) {
        GameEntity game = gameRepo.findByIdAndActiveTrue(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        JsonNode config = activeConfigNode(game);
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

    /** Saldo del jugador autenticado. */
    public WalletDto getWallet(Long userId) {
        return walletRepo.findByUserId(userId)
                .map(w -> new WalletDto(w.getBalanceCents(), w.getCurrency()))
                .orElseThrow(() -> new IllegalStateException("Wallet no encontrado para el usuario " + userId));
    }

    // -------------------------------------------------------------------------

    /** `config` completo de la versión activa como árbol JSON. */
    private JsonNode activeConfigNode(GameEntity game) {
        if (game.getActiveConfigId() == null) {
            // Sin matemática activa el juego no es jugable → se trata como no disponible.
            throw new GameNotFoundException(game.getId());
        }
        GameConfigEntity cfg = configRepo.findById(game.getActiveConfigId())
                .orElseThrow(() -> new GameNotFoundException(game.getId()));
        return parse(cfg.getConfig());
    }

    /** Solo el nodo `grid` del config activo (para el lobby); null si no hay config. */
    private JsonNode gridOf(GameEntity game) {
        if (game.getActiveConfigId() == null) return null;
        return configRepo.findById(game.getActiveConfigId())
                .map(cfg -> parse(cfg.getConfig()).get("grid"))
                .orElse(null);
    }

    private JsonNode parse(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("config JSON inválido en BBDD", e);
        }
    }
}
