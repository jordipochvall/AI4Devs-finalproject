package com.novacasino.application.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

/** Output port for refresh-token persistence (HU-13). Implemented by an adapter in infrastructure. */
public interface RefreshTokenStorePort {

    /** Persists a new refresh token (only its hash is stored). */
    void save(Long userId, String tokenHash, OffsetDateTime expiresAt);

    /** Looks up a token by the SHA-256 hash of its opaque value. */
    Optional<StoredRefreshToken> findByHash(String tokenHash);

    /** Marks a token as revoked. */
    void markRevoked(Long id);
}
