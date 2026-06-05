package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

/**
 * Vista de lectura del catálogo de juegos. Solo mapea las columnas que necesitan
 * los endpoints del jugador (no la configuración comercial completa ni timestamps).
 */
@Entity
@Table(name = "games")
public class GameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 20)
    private String theme;

    @Column(name = "cover_image_url", nullable = false, length = 255)
    private String coverImageUrl;

    @Column(name = "min_bet_cents", nullable = false)
    private long minBetCents;

    @Column(name = "max_bet_cents", nullable = false)
    private long maxBetCents;

    @Column(name = "bet_step_cents", nullable = false)
    private long betStepCents;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "active_config_id")
    private Long activeConfigId;

    public Long getId()             { return id; }
    public Long getOperatorId()     { return operatorId; }
    public String getCode()         { return code; }
    public String getName()         { return name; }
    public String getTheme()        { return theme; }
    public String getCoverImageUrl(){ return coverImageUrl; }
    public long getMinBetCents()    { return minBetCents; }
    public long getMaxBetCents()    { return maxBetCents; }
    public long getBetStepCents()   { return betStepCents; }
    public boolean isActive()       { return active; }
    public Long getActiveConfigId() { return activeConfigId; }
}
