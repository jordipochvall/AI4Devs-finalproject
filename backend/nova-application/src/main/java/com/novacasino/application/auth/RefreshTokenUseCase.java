package com.novacasino.application.auth;

import com.novacasino.application.auth.exception.InvalidRefreshTokenException;
import jakarta.transaction.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * Issues, rotates and revokes opaque refresh tokens (HU-13). Pure application use case: depends only
 * on {@link RefreshTokenStorePort}. The token returned to the client is a 256-bit random value; only
 * its SHA-256 hash is persisted. Rotation: {@link #consume} validates a token and revokes it.
 */
public class RefreshTokenUseCase {

    private final RefreshTokenStorePort store;
    private final long ttlSeconds;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenUseCase(final RefreshTokenStorePort store, final long ttlSeconds) {
        this.store = store;
        this.ttlSeconds = ttlSeconds;
    }

    /** Issues a new refresh token for a user and returns its opaque value (the hash is stored). */
    @Transactional
    public String issue(final Long userId) {
        final String token = randomToken();
        store.save(userId, sha256(token), OffsetDateTime.now().plusSeconds(ttlSeconds));
        return token;
    }

    /**
     * Validates a refresh token and revokes it (rotation), returning its owner. Throws
     * {@link InvalidRefreshTokenException} if it is unknown, expired or already revoked.
     */
    @Transactional
    public Long consume(final String token) {
        final StoredRefreshToken stored = store.findByHash(sha256(token))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!stored.isActive()) {
            throw new InvalidRefreshTokenException();
        }
        store.markRevoked(stored.id());
        return stored.userId();
    }

    /** Revokes a token if present (logout); a no-op when the token is unknown. */
    @Transactional
    public void revoke(final String token) {
        store.findByHash(sha256(token)).ifPresent(stored -> store.markRevoked(stored.id()));
    }

    // -------------------------------------------------------------------------

    private String randomToken() {
        final byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(final String input) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(64);
            for (final byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
