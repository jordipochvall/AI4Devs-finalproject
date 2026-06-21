package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/**
 * Progressive jackpot pool for a game (HU-26). Mutable: the current value grows with each bet's
 * contribution and resets to the seed when granted. Optimistic locking (@Version) serializes
 * concurrent spins on the same pool.
 */
@Entity
@Table(name = "jackpot_pools")
public class JackpotPoolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "game_id", nullable = false, updatable = false)
    private Long gameId;

    @Column(name = "current_cents", nullable = false)
    private long currentCents;

    @Column(name = "seed_cents", nullable = false)
    private long seedCents;

    @Column(name = "contribution_bps", nullable = false)
    private int contributionBps;

    @Column(name = "odds_denominator", nullable = false)
    private long oddsDenominator;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected JackpotPoolEntity() { }

    public JackpotPoolEntity(final Long gameId, final long currentCents, final long seedCents,
                             final int contributionBps, final long oddsDenominator) {
        this.gameId = gameId;
        this.currentCents = currentCents;
        this.seedCents = seedCents;
        this.contributionBps = contributionBps;
        this.oddsDenominator = oddsDenominator;
    }

    public Long getId()              { return id; }
    public Long getGameId()          { return gameId; }
    public long getCurrentCents()    { return currentCents; }
    public long getSeedCents()       { return seedCents; }
    public int getContributionBps()  { return contributionBps; }
    public long getOddsDenominator() { return oddsDenominator; }

    public void setCurrentCents(final long currentCents) { this.currentCents = currentCents; }
    public void setUpdatedAt(final OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
