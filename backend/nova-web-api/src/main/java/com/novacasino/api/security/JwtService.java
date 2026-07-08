package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Creates and validates JWT Bearer tokens (HS256). The JWT secret is normalised to 256 bits
 * via SHA-256, but the source secret itself must already carry at least 32 bytes of entropy
 * (HU-36): a short or example secret (e.g. the placeholder in {@code .env.example}) makes the
 * derived key trivially guessable regardless of its hashed length, so the app refuses to start
 * rather than silently signing sessions with a weak secret.
 */
@Service
public class JwtService {

    /** Minimum entropy of app.jwt.secret (HU-36): fail-fast at startup below this. */
    static final int MIN_SECRET_BYTES = 32;

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.ttl-seconds}")
    private long ttlSeconds;

    @PostConstruct
    void validateSecret() {
        final int length = secret == null ? 0 : secret.getBytes(StandardCharsets.UTF_8).length;
        if (length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) must be at least " + MIN_SECRET_BYTES
                            + " bytes long, got " + length
                            + ". Generate one with: openssl rand -base64 48");
        }
    }

    /** Issues a signed JWT carrying the user's id (subject), email and role. */
    public String generateToken(final UserEntity user) {
        final Date now = new Date();
        final Date expiry = new Date(now.getTime() + ttlSeconds * 1000L);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role",  user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    /** Extracts the email claim from a token (no validity check). */
    public String extractEmail(final String token) {
        return parseClaims(token).get("email", String.class);
    }

    /** Returns whether the token's signature is valid and it has not expired. */
    public boolean isValid(final String token) {
        try {
            final Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (final JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    private Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Derives a 256-bit HMAC key from the configured secret via SHA-256. */
    private SecretKey signingKey() {
        try {
            final MessageDigest sha = MessageDigest.getInstance("SHA-256");
            final byte[] keyBytes = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
