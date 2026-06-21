package com.novacasino.api.auth;

import com.novacasino.api.auth.exception.InvalidRefreshTokenException;
import com.novacasino.infrastructure.persistence.entity.RefreshTokenEntity;
import com.novacasino.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * Issues, rotates and revokes opaque refresh tokens (HU-13). The token returned to the client is a
 * 256-bit random value; only its SHA-256 hash is persisted, so a database leak does not expose usable
 * tokens. Rotation: {@link #consume} validates a token and revokes it, and the caller issues a fresh
 * one — a revoked token can never be reused.
 */
@Service
public class RefreshTokenService {

    private final RefreshTokenJpaRepository repo;
    private final long ttlSeconds;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(final RefreshTokenJpaRepository repo,
                               @Value("${app.jwt.refresh-ttl-seconds:2592000}") final long ttlSeconds) {
        this.repo = repo;
        this.ttlSeconds = ttlSeconds;
    }

    /** Issues a new refresh token for a user and returns its opaque value (the hash is stored). */
    @Transactional
    public String issue(final Long userId) {
        final String token = randomToken();
        repo.save(new RefreshTokenEntity(userId, sha256(token),
                OffsetDateTime.now().plusSeconds(ttlSeconds)));
        return token;
    }

    /**
     * Validates a refresh token and revokes it (rotation), returning its owner. Throws
     * {@link InvalidRefreshTokenException} if it is unknown, expired or already revoked.
     */
    @Transactional
    public Long consume(final String token) {
        final RefreshTokenEntity entity = repo.findByTokenHash(sha256(token))
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!entity.isActive()) {
            throw new InvalidRefreshTokenException();
        }
        entity.setRevoked(true);
        repo.save(entity);
        return entity.getUserId();
    }

    /** Revokes a token if present (logout); a no-op when the token is unknown. */
    @Transactional
    public void revoke(final String token) {
        repo.findByTokenHash(sha256(token)).ifPresent(entity -> {
            entity.setRevoked(true);
            repo.save(entity);
        });
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
