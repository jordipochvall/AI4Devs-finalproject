package com.novacasino.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Request to revoke a refresh token on logout (HU-13). */
public record LogoutRequest(@NotBlank String refreshToken) {
}
