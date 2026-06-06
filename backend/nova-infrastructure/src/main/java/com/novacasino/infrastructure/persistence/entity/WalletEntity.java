package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Wallet JPA entity. One row per player; balance in cents. Uses optimistic locking
 * ({@code @Version}) to prevent lost updates between spin and operator recharge.
 * {@code updated_at} is managed via {@code @PrePersist}/{@code @PreUpdate}.
 */
@Entity
@Table(name = "wallets")
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "balance_cents", nullable = false)
    private long balanceCents = 0L;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Version
    @Column(nullable = false)
    private long version = 0L;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    /** Keeps {@code updated_at} current on insert and update. */
    @PrePersist
    @PreUpdate
    void touch() { updatedAt = OffsetDateTime.now(); }

    // --- Getters ---
    public Long getId()             { return id; }
    public Long getOperatorId()     { return operatorId; }
    public Long getUserId()         { return userId; }
    public long getBalanceCents()   { return balanceCents; }
    public String getCurrency()     { return currency; }
    public long getVersion()        { return version; }

    // --- Setters ---
    public void setOperatorId(final Long operatorId) { this.operatorId = operatorId; }
    public void setUserId(final Long userId)         { this.userId = userId; }
    public void setBalanceCents(final long cents)    { this.balanceCents = cents; }
    public void setCurrency(final String currency)   { this.currency = currency; }
}
