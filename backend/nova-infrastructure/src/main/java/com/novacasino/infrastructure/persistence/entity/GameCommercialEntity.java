package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Write-enabled view of a game's <b>commercial</b> configuration (HU-15), mapped onto the same
 * {@code games} table as {@link GameEntity}. Kept separate so the engine's read-only {@link GameEntity}
 * stays untouched (it never needs the {@code allowed_currencies} array). Only commercial columns are
 * mutable here; math lives in {@code game_configs}.
 */
@Entity
@Table(name = "games")
public class GameCommercialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false, updatable = false)
    private Long operatorId;

    @Column(nullable = false, updatable = false, length = 50)
    private String code;

    @Column(nullable = false, updatable = false, length = 100)
    private String name;

    @Column(nullable = false, updatable = false, length = 20)
    private String theme;

    @Column(name = "min_bet_cents", nullable = false)
    private long minBetCents;

    @Column(name = "max_bet_cents", nullable = false)
    private long maxBetCents;

    @Column(name = "bet_step_cents", nullable = false)
    private long betStepCents;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "allowed_currencies", nullable = false, columnDefinition = "char(3)[]")
    private String[] allowedCurrencies;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "active_config_id", updatable = false)
    private Long activeConfigId;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public Long getId()                  { return id; }
    public Long getOperatorId()          { return operatorId; }
    public String getCode()              { return code; }
    public String getName()              { return name; }
    public String getTheme()             { return theme; }
    public long getMinBetCents()         { return minBetCents; }
    public long getMaxBetCents()         { return maxBetCents; }
    public long getBetStepCents()        { return betStepCents; }
    public String[] getAllowedCurrencies() { return allowedCurrencies; }
    public boolean isActive()            { return active; }
    public Long getActiveConfigId()      { return activeConfigId; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setMinBetCents(final long minBetCents)   { this.minBetCents = minBetCents; }
    public void setMaxBetCents(final long maxBetCents)   { this.maxBetCents = maxBetCents; }
    public void setBetStepCents(final long betStepCents) { this.betStepCents = betStepCents; }
    public void setAllowedCurrencies(final String[] allowedCurrencies) { this.allowedCurrencies = allowedCurrencies; }
    public void setActive(final boolean active)          { this.active = active; }
    public void setUpdatedAt(final OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
