package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * A responsible-gaming limit for a player (HU-19): one row per (user, type, period). Mutable —
 * hardening updates {@code amount} immediately; relaxing stages {@code pending_*} until the cooldown.
 */
@Entity
@Table(name = "player_limits")
public class PlayerLimitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "limit_type", nullable = false, length = 20)
    private String limitType;

    @Column(nullable = false, length = 20)
    private String period;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "effective_at", nullable = false)
    private OffsetDateTime effectiveAt;

    @Column(name = "pending_amount_cents")
    private Long pendingAmountCents;

    @Column(name = "pending_effective_at")
    private OffsetDateTime pendingEffectiveAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected PlayerLimitEntity() { }

    public PlayerLimitEntity(final Long userId, final String limitType, final String period,
                             final long amountCents, final OffsetDateTime effectiveAt) {
        this.userId      = userId;
        this.limitType   = limitType;
        this.period      = period;
        this.amountCents = amountCents;
        this.effectiveAt = effectiveAt;
        this.updatedAt   = effectiveAt;
    }

    public Long getId()                       { return id; }
    public Long getUserId()                   { return userId; }
    public String getLimitType()              { return limitType; }
    public String getPeriod()                 { return period; }
    public long getAmountCents()              { return amountCents; }
    public OffsetDateTime getEffectiveAt()    { return effectiveAt; }
    public Long getPendingAmountCents()       { return pendingAmountCents; }
    public OffsetDateTime getPendingEffectiveAt() { return pendingEffectiveAt; }
    public OffsetDateTime getCreatedAt()      { return createdAt; }
    public OffsetDateTime getUpdatedAt()      { return updatedAt; }

    public void setAmountCents(final long amountCents)            { this.amountCents = amountCents; }
    public void setEffectiveAt(final OffsetDateTime v)            { this.effectiveAt = v; }
    public void setPendingAmountCents(final Long v)              { this.pendingAmountCents = v; }
    public void setPendingEffectiveAt(final OffsetDateTime v)     { this.pendingEffectiveAt = v; }
    public void setUpdatedAt(final OffsetDateTime v)             { this.updatedAt = v; }
}
