package com.novacasino.api.math.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request to launch a mass simulation. The range of {@code numSpins} (1..10_000_000) and the
 * payline-multiple rule for {@code betCents} are business validations done in the service (returning
 * 422), so only their presence is enforced here.
 */
public record LaunchSimulationRequest(
        @NotNull Long numSpins,
        @NotNull Long betCents
) {}
