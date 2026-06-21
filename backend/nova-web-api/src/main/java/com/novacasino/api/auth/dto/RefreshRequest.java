package com.novacasino.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Request to renew the session from a valid refresh token (HU-13). */
public record RefreshRequest(@NotBlank String refreshToken) {
}
