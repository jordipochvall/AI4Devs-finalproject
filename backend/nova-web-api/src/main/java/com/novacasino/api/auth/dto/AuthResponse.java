package com.novacasino.api.auth.dto;

/** Authentication response: a JWT plus the authenticated user's public data. */
public record AuthResponse(
        String  token,
        String  tokenType,
        long    expiresIn,
        UserDto user
) {
    /** Convenience constructor that fixes the token type to "Bearer". */
    public AuthResponse(final String token, final long expiresIn, final UserDto user) {
        this(token, "Bearer", expiresIn, user);
    }
}
