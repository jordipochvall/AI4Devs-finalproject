package com.novacasino.application.player;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerRoundDto;
import com.novacasino.common.dto.WalletTransactionDto;

import java.util.List;

/**
 * Read-only player history (HU-14): the player's own wallet movements and rounds, paginated and
 * strictly scoped to the authenticated player. Pure use case over {@link PlayerHistoryPort}.
 */
public class PlayerHistoryUseCase {

    private final PlayerHistoryPort history;

    public PlayerHistoryUseCase(final PlayerHistoryPort history) {
        this.history = history;
    }

    /** The player's wallet ledger movements (empty page if the player has no wallet). */
    public PageResponse<WalletTransactionDto> listTransactions(final Long userId, final PageRequestDto page) {
        return history.walletIdOf(userId)
                .map(walletId -> history.transactionsByWallet(walletId, page))
                .orElseGet(() -> new PageResponse<>(List.of(), page.page(), page.size(), 0, 0));
    }

    /** The player's own rounds. */
    public PageResponse<PlayerRoundDto> listRounds(final Long userId, final PageRequestDto page) {
        return history.roundsByPlayer(userId, page);
    }
}
