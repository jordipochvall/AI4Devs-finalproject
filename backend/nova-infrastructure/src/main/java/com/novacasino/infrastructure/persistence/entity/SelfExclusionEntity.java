package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/** A self-exclusion period for a player (HU-19): play is blocked while {@code now} is before end. */
@Entity
@Table(name = "self_exclusions")
public class SelfExclusionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "start_at", nullable = false)
    private OffsetDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private OffsetDateTime endAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SelfExclusionEntity() { }

    public SelfExclusionEntity(final Long userId, final OffsetDateTime startAt, final OffsetDateTime endAt) {
        this.userId  = userId;
        this.startAt = startAt;
        this.endAt   = endAt;
    }

    public Long getId()                { return id; }
    public Long getUserId()            { return userId; }
    public OffsetDateTime getStartAt() { return startAt; }
    public OffsetDateTime getEndAt()   { return endAt; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
