package com.novacasino.api.operator.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * Commercial-configuration update (HU-15). Bet bounds and active flag are required; currencies are
 * optional (kept unchanged when omitted). Cross-field coherence (max ≥ min, payline multiples) is
 * validated in the service.
 */
public record UpdateGameRequest(
        @NotNull @Positive Long minBetCents,
        @NotNull @Positive Long maxBetCents,
        @NotNull @Positive Long betStepCents,
        @NotNull Boolean active,
        List<String> allowedCurrencies
) {
}
