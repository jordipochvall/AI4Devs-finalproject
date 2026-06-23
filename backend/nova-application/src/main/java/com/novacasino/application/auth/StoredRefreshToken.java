package com.novacasino.application.auth;

import java.time.OffsetDateTime;

/** Application view of a stored refresh token (HU-13); the adapter maps the JPA row into this. */
public record StoredRefreshToken(Long id, Long userId, OffsetDateTime expiresAt, boolean revoked) {

    /** Usable only if neither revoked nor past its expiry. */
    public boolean isActive() {
        return !revoked && expiresAt.isAfter(OffsetDateTime.now());
    }
}
