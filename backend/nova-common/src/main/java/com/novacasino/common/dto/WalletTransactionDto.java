package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/**
 * One wallet ledger movement in the player's own history (HU-14).
 *
 * @param id                 movement id
 * @param type               RECHARGE / BET / WIN
 * @param amountCents         signed amount (positive for RECHARGE/WIN, negative for BET)
 * @param balanceAfterCents   resulting balance after the movement
 * @param gameRoundId         the related round (BET/WIN only; null for RECHARGE)
 * @param createdAt           when the movement was recorded
 */
public record WalletTransactionDto(
        Long id,
        String type,
        long amountCents,
        long balanceAfterCents,
        Long gameRoundId,
        OffsetDateTime createdAt) {
}
