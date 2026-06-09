package com.novacasino.api.player.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Spin request body. {@code betCents} is the total bet; its range, payline-multiple and balance
 * checks are business rules validated in the service (returning 422), not bean-validation format
 * errors — only its presence is required here.
 */
public record SpinRequest(
        @NotNull Long betCents,
        String currency
) {}
