package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Append-only audit of a commercial configuration change (HU-15). Stores a JSON snapshot of the
 * commercial fields before and after the {@code PUT /operator/games/{id}}, plus author and timestamp.
 * Immutable: a DB trigger forbids UPDATE/DELETE.
 */
@Entity
@Table(name = "game_commercial_audits")
public class GameCommercialAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "performed_by_user_id", nullable = false)
    private Long performedByUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_value", nullable = false, columnDefinition = "jsonb")
    private String beforeValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_value", nullable = false, columnDefinition = "jsonb")
    private String afterValue;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    protected GameCommercialAuditEntity() { }

    public GameCommercialAuditEntity(final Long operatorId, final Long gameId,
                                     final Long performedByUserId,
                                     final String beforeValue, final String afterValue) {
        this.operatorId        = operatorId;
        this.gameId            = gameId;
        this.performedByUserId = performedByUserId;
        this.beforeValue       = beforeValue;
        this.afterValue        = afterValue;
        this.createdAt         = OffsetDateTime.now();
    }

    public Long getId()                  { return id; }
    public Long getOperatorId()          { return operatorId; }
    public Long getGameId()              { return gameId; }
    public Long getPerformedByUserId()   { return performedByUserId; }
    public String getBeforeValue()       { return beforeValue; }
    public String getAfterValue()        { return afterValue; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
