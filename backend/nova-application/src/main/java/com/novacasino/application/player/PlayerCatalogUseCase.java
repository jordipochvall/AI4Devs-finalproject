package com.novacasino.application.player;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.common.dto.GameDetailDto;
import com.novacasino.common.dto.GameSummaryDto;
import com.novacasino.common.dto.WalletDto;

import java.util.List;

/**
 * Player read use cases (HU-5): lobby catalogue, game detail (+ config) and balance. Read-only — no
 * {@code @Transactional} here so the read runs in the read-only transaction owned by the adapter
 * (the writes that matter are guarded one layer down).
 */
public class PlayerCatalogUseCase {

    private final PlayerCatalogPort catalog;

    public PlayerCatalogUseCase(final PlayerCatalogPort catalog) {
        this.catalog = catalog;
    }

    public List<GameSummaryDto> listActiveGames() {
        return catalog.listActiveGames();
    }

    public GameDetailDto getActiveGame(final Long gameId) {
        return catalog.activeGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    public WalletDto getWallet(final Long userId) {
        return catalog.walletOf(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
    }
}
