package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/** Immutable record of a jackpot being granted on a round (HU-26). A DB trigger forbids UPDATE/DELETE. */
@Entity
@Table(name = "jackpot_grants")
public class JackpotGrantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "game_round_id", nullable = false)
    private Long gameRoundId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected JackpotGrantEntity() { }

    public JackpotGrantEntity(final Long operatorId, final Long gameId, final Long gameRoundId,
                              final Long playerId, final long amountCents) {
        this.operatorId = operatorId;
        this.gameId = gameId;
        this.gameRoundId = gameRoundId;
        this.playerId = playerId;
        this.amountCents = amountCents;
    }

    public Long getId()            { return id; }
    public long getAmountCents()   { return amountCents; }
    public Long getGameRoundId()   { return gameRoundId; }
}
