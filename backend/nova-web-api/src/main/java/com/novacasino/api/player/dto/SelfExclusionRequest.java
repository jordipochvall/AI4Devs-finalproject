package com.novacasino.api.player.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Request to self-exclude for a number of days (HU-19). */
public record SelfExclusionRequest(
        @NotNull @Positive @Max(3650) Integer days) {
}
