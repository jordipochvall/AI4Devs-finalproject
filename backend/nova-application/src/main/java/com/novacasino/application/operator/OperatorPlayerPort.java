package com.novacasino.application.operator;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerSummaryDto;
import com.novacasino.common.dto.WalletDto;

import java.util.Optional;

/** Output port for operator player management (HU-6): search and wallet recharge. */
public interface OperatorPlayerPort {

    PageResponse<PlayerSummaryDto> searchPlayers(Long operatorId, String email, PageRequestDto page);

    /** Credits the player's wallet and records the RECHARGE movement; empty if the player is not found. */
    Optional<WalletDto> recharge(Long operatorUserId, Long operatorId, Long playerId, long amountCents);
}
