package com.novacasino.application.player;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.common.dto.GameDetailDto;
import com.novacasino.common.dto.GameSummaryDto;
import com.novacasino.common.dto.WalletDto;
import jakarta.transaction.Transactional;

import java.util.List;

/** Player read use cases (HU-5): lobby catalogue, game detail (+ config) and balance. */
public class PlayerCatalogUseCase {

    private final PlayerCatalogPort catalog;

    public PlayerCatalogUseCase(final PlayerCatalogPort catalog) {
        this.catalog = catalog;
    }

    @Transactional
    public List<GameSummaryDto> listActiveGames() {
        return catalog.listActiveGames();
    }

    @Transactional
    public GameDetailDto getActiveGame(final Long gameId) {
        return catalog.activeGame(gameId).orElseThrow(() -> new GameNotFoundException(gameId));
    }

    @Transactional
    public WalletDto getWallet(final Long userId) {
        return catalog.walletOf(userId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found for user " + userId));
    }
}
