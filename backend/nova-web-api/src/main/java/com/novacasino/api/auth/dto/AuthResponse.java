package com.novacasino.api.auth.dto;

/** Authentication response: a JWT (+ refresh token) plus the authenticated user's public data. */
public record AuthResponse(
        String  token,
        String  tokenType,
        long    expiresIn,
        String  refreshToken,
        UserDto user
) {
    /** Convenience constructor that fixes the token type to "Bearer". */
    public AuthResponse(final String token, final long expiresIn, final String refreshToken,
                        final UserDto user) {
        this(token, "Bearer", expiresIn, refreshToken, user);
    }
}
