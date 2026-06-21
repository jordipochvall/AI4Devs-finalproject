package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * A refresh token (HU-13). The opaque token value is never stored: only its SHA-256 hash. Mutable —
 * the {@code revoked} flag flips on rotation/logout, so there is no immutability trigger.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected RefreshTokenEntity() { }

    public RefreshTokenEntity(final Long userId, final String tokenHash, final OffsetDateTime expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    public Long getId()              { return id; }
    public Long getUserId()          { return userId; }
    public String getTokenHash()     { return tokenHash; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isRevoked()       { return revoked; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setRevoked(final boolean revoked) { this.revoked = revoked; }

    /** A token is usable only if it is neither revoked nor past its expiry. */
    public boolean isActive() {
        return !revoked && expiresAt.isAfter(OffsetDateTime.now());
    }
}
