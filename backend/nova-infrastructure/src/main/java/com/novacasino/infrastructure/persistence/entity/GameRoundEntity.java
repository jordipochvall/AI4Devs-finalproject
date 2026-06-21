package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * Auditable record of a single spin (append-only, DGOJ): a DB trigger forbids UPDATE/DELETE.
 * Free spins are stored as child rows linked to the triggering round via {@code triggeringRoundId}.
 *
 * <p>The {@code result} JSONB holds only the visual/mechanical part of the spin
 * ({@code view}, {@code winningPaylines}, {@code scatterCount}, {@code multiplier}); the amounts live
 * in dedicated columns (readme §3.2.8).
 */
@Entity
@Table(name = "game_rounds")
public class GameRoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "game_config_id", nullable = false)
    private Long gameConfigId;

    @Column(name = "rng_seed", nullable = false)
    private long rngSeed;

    @Column(name = "bet_cents", nullable = false)
    private long betCents;

    @Column(name = "win_cents", nullable = false)
    private long winCents;

    @Column(name = "balance_pre_cents", nullable = false)
    private long balancePreCents;

    @Column(name = "balance_post_cents", nullable = false)
    private long balancePostCents;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String result;

    @Column(name = "is_free_spin", nullable = false)
    private boolean freeSpin;

    @Column(name = "triggering_round_id")
    private Long triggeringRoundId;

    @Column(name = "free_spins_remaining_after")
    private Integer freeSpinsRemainingAfter;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Integrity chain (HU-20): set by a DB trigger on insert; never written by the app. */
    @Column(name = "prev_hash", insertable = false, updatable = false)
    private String prevHash;

    @Column(name = "row_hash", insertable = false, updatable = false)
    private String rowHash;

    protected GameRoundEntity() { }

    private GameRoundEntity(final Long operatorId, final Long playerId, final Long gameId,
                           final Long gameConfigId, final long rngSeed, final long betCents,
                           final long winCents, final long balancePreCents, final long balancePostCents,
                           final String result, final boolean freeSpin, final Long triggeringRoundId,
                           final Integer freeSpinsRemainingAfter) {
        this.operatorId = operatorId;
        this.playerId = playerId;
        this.gameId = gameId;
        this.gameConfigId = gameConfigId;
        this.rngSeed = rngSeed;
        this.betCents = betCents;
        this.winCents = winCents;
        this.balancePreCents = balancePreCents;
        this.balancePostCents = balancePostCents;
        this.result = result;
        this.freeSpin = freeSpin;
        this.triggeringRoundId = triggeringRoundId;
        this.freeSpinsRemainingAfter = freeSpinsRemainingAfter;
    }

    /**
     * Builds the base round of a spin (not a free spin).
     *
     * @param result the mechanical result JSON of the base spin
     */
    public static GameRoundEntity baseRound(final Long operatorId, final Long playerId, final Long gameId,
                                            final Long gameConfigId, final long rngSeed, final long betCents,
                                            final long winCents, final long balancePreCents,
                                            final long balancePostCents, final String result) {
        return new GameRoundEntity(operatorId, playerId, gameId, gameConfigId, rngSeed, betCents,
                winCents, balancePreCents, balancePostCents, result, false, null, null);
    }

    /**
     * Builds a free-spin child round linked to its triggering round.
     *
     * @param triggeringRoundId        id of the base round that triggered the free spins
     * @param freeSpinsRemainingAfter  free spins still pending after this one
     */
    public static GameRoundEntity freeSpinRound(final Long operatorId, final Long playerId, final Long gameId,
                                                final Long gameConfigId, final long rngSeed, final long winCents,
                                                final long balancePreCents, final long balancePostCents,
                                                final String result, final Long triggeringRoundId,
                                                final int freeSpinsRemainingAfter) {
        return new GameRoundEntity(operatorId, playerId, gameId, gameConfigId, rngSeed, 0L,
                winCents, balancePreCents, balancePostCents, result, true, triggeringRoundId,
                freeSpinsRemainingAfter);
    }

    public Long getId()                     { return id; }
    public Long getOperatorId()             { return operatorId; }
    public Long getPlayerId()               { return playerId; }
    public Long getGameId()                 { return gameId; }
    public Long getGameConfigId()           { return gameConfigId; }
    public long getRngSeed()                { return rngSeed; }
    public long getBetCents()               { return betCents; }
    public long getWinCents()               { return winCents; }
    public long getBalancePreCents()        { return balancePreCents; }
    public long getBalancePostCents()       { return balancePostCents; }
    public String getResult()               { return result; }
    public boolean isFreeSpin()             { return freeSpin; }
    public Long getTriggeringRoundId()      { return triggeringRoundId; }
    public Integer getFreeSpinsRemainingAfter() { return freeSpinsRemainingAfter; }
    public OffsetDateTime getCreatedAt()    { return createdAt; }
    public String getPrevHash()             { return prevHash; }
    public String getRowHash()              { return rowHash; }
}
