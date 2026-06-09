package com.novacasino.api.math.dto;

import jakarta.validation.constraints.NotBlank;

/** A natural-language question about a simulation's results (HU-8). */
public record ExplainRequest(@NotBlank String question) {
}
