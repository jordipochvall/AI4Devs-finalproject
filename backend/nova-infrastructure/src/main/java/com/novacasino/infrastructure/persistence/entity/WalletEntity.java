package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

// updated_at se gestiona vía @PrePersist/@PreUpdate; no depende de DEFAULT NOW() de la DB.

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
    public void setOperatorId(Long operatorId)  { this.operatorId = operatorId; }
    public void setUserId(Long userId)          { this.userId = userId; }
    public void setBalanceCents(long cents)     { this.balanceCents = cents; }
    public void setCurrency(String currency)    { this.currency = currency; }
}
