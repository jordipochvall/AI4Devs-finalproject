package com.novacasino.api.operator.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Recharge request. {@code amountCents} must be positive; this is checked in the service
 * (not with @Positive) so it returns a business 422 rather than a format 400 (AC3).
 */
public record RechargeRequest(
        @NotNull Long amountCents,
        String currency
) {}
