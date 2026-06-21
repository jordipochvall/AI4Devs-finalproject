package com.novacasino.api.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request to set a responsible-gaming limit (HU-19).
 *
 * @param limitType  LOSS / DEPOSIT / SESSION_TIME
 * @param period     DAILY / WEEKLY / MONTHLY
 * @param amountCents the limit value in cents (loss/deposit) or minutes encoded as cents (time)
 */
public record SetLimitRequest(
        @NotBlank String limitType,
        @NotBlank String period,
        @NotNull @PositiveOrZero Long amountCents) {
}
