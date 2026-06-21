package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Immutable record of a math version being published (activated) for a game (HU-17). A new row is
 * inserted on every publish; rows are never modified (a DB trigger forbids UPDATE/DELETE).
 */
@Entity
@Table(name = "game_config_publications")
public class GameConfigPublicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "game_config_id", nullable = false)
    private Long gameConfigId;

    @Column(name = "published_by_user_id", nullable = false)
    private Long publishedByUserId;

    @Column(name = "published_at", updatable = false)
    private OffsetDateTime publishedAt;

    protected GameConfigPublicationEntity() { }

    public GameConfigPublicationEntity(final Long operatorId, final Long gameId,
                                       final Long gameConfigId, final Long publishedByUserId) {
        this.operatorId        = operatorId;
        this.gameId            = gameId;
        this.gameConfigId      = gameConfigId;
        this.publishedByUserId = publishedByUserId;
        this.publishedAt       = OffsetDateTime.now();
    }

    public Long getId()                  { return id; }
    public Long getOperatorId()          { return operatorId; }
    public Long getGameId()              { return gameId; }
    public Long getGameConfigId()        { return gameConfigId; }
    public Long getPublishedByUserId()   { return publishedByUserId; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
}
