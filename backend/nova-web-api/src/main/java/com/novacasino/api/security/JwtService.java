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
 * Crea y valida JWT Bearer tokens (HS256).
 * El JWT_SECRET se normaliza a 256 bits via SHA-256 para tolerar valores cortos en dev.
 * En producción usar un secreto de mínimo 32 bytes de entropía.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.ttl-seconds}")
    private long ttlSeconds;

    public String generateToken(UserEntity user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlSeconds * 1000L);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role",  user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getTtlSeconds() { return ttlSeconds; }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        try {
            // SHA-256 del secreto → siempre 256 bits independientemente de su longitud.
            // Suficiente para dev; en producción usar un secreto de ≥32 bytes de entropía.
            byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 32) {
                log.warn("JWT_SECRET es más corto de 32 bytes; se recomienda una longitud mayor en producción.");
            }
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(raw);
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
