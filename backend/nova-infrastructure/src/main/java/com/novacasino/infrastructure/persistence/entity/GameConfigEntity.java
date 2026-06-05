package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Versión de la matemática de un juego. Para los endpoints del jugador solo se lee
 * el {@code config} (JSON crudo) de la versión activa; la matemática se interpreta
 * en cliente/motor, no aquí.
 */
@Entity
@Table(name = "game_configs")
public class GameConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(nullable = false)
    private int version;

    /** JSONB → se lee como texto JSON crudo; el servicio lo parsea a árbol JSON. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String config;

    public Long getId()      { return id; }
    public Long getGameId()  { return gameId; }
    public int getVersion()  { return version; }
    public String getConfig(){ return config; }
}
