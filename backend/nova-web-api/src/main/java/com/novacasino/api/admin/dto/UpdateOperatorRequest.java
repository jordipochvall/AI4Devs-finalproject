package com.novacasino.api.admin.dto;

import jakarta.validation.constraints.NotNull;

/** Request to activate/deactivate an operator (HU-25). */
public record UpdateOperatorRequest(@NotNull Boolean active) {
}
