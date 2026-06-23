package com.novacasino.application.player;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerRoundDto;
import com.novacasino.common.dto.WalletTransactionDto;

import java.util.Optional;

/** Output port for the player's read-only history (HU-14). Implemented by an infrastructure adapter. */
public interface PlayerHistoryPort {

    /** The player's wallet id, if they have a wallet. */
    Optional<Long> walletIdOf(Long userId);

    /** Ledger movements of a wallet, newest first. */
    PageResponse<WalletTransactionDto> transactionsByWallet(Long walletId, PageRequestDto page);

    /** The player's own rounds, newest first. */
    PageResponse<PlayerRoundDto> roundsByPlayer(Long userId, PageRequestDto page);
}
