package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * via SHA-256 so short values still work in development. In production use a secret with at
 * least 32 bytes of entropy.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.ttl-seconds}")
    private long ttlSeconds;

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
            final byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                log.warn("JWT_SECRET is shorter than 32 bytes; use a longer secret in production.");
            }
            final MessageDigest sha = MessageDigest.getInstance("SHA-256");
            final byte[] keyBytes = sha.digest(raw);
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
