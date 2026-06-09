package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

/**
 * A mass-simulation run (readme §3.2.9). Inserted as {@code RUNNING}, then updated to
 * {@code COMPLETED} with the aggregated metrics (or {@code FAILED} with an error message) when the
 * asynchronous execution finishes. The detailed metrics (histogram, convergence curve, RTP
 * breakdown) are stored as {@code JSONB}.
 */
@Entity
@Table(name = "simulation_runs")
public class SimulationRunEntity {

    public static final String RUNNING = "RUNNING";
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "game_config_id", nullable = false)
    private Long gameConfigId;

    @Column(name = "launched_by_user_id", nullable = false)
    private Long launchedByUserId;

    @Column(name = "num_spins", nullable = false)
    private long numSpins;

    @Column(name = "bet_cents", nullable = false)
    private long betCents;

    @Column(nullable = false, length = 20)
    private String status = RUNNING;

    @Column(name = "rtp_empirical")
    private Double rtpEmpirical;

    @Column(name = "rtp_std_error")
    private Double rtpStdError;

    @Column(name = "rtp_base_game")
    private Double rtpBaseGame;

    @Column(name = "rtp_free_spins")
    private Double rtpFreeSpins;

    @Column(name = "hit_frequency")
    private Double hitFrequency;

    @Column(name = "volatility")
    private Double volatility;

    @Column(name = "max_win_multiplier")
    private Double maxWinMultiplier;

    @Column(name = "free_spin_trigger_freq")
    private Double freeSpinTriggerFreq;

    @Column(name = "longest_dry_streak")
    private Integer longestDryStreak;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prize_distribution", columnDefinition = "jsonb")
    private String prizeDistribution;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "convergence_sample", columnDefinition = "jsonb")
    private String convergenceSample;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "rtp_breakdown", columnDefinition = "jsonb")
    private String rtpBreakdown;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "started_at", insertable = false, updatable = false)
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected SimulationRunEntity() { }

    /** Creates a RUNNING run to be inserted before the asynchronous execution starts. */
    public SimulationRunEntity(final Long operatorId, final Long gameConfigId,
                               final Long launchedByUserId, final long numSpins, final long betCents) {
        this.operatorId = operatorId;
        this.gameConfigId = gameConfigId;
        this.launchedByUserId = launchedByUserId;
        this.numSpins = numSpins;
        this.betCents = betCents;
        this.status = RUNNING;
    }

    public Long getId()                  { return id; }
    public Long getOperatorId()          { return operatorId; }
    public Long getGameConfigId()        { return gameConfigId; }
    public Long getLaunchedByUserId()    { return launchedByUserId; }
    public long getNumSpins()            { return numSpins; }
    public long getBetCents()            { return betCents; }
    public String getStatus()            { return status; }
    public Double getRtpEmpirical()      { return rtpEmpirical; }
    public Double getRtpStdError()       { return rtpStdError; }
    public Double getRtpBaseGame()       { return rtpBaseGame; }
    public Double getRtpFreeSpins()      { return rtpFreeSpins; }
    public Double getHitFrequency()      { return hitFrequency; }
    public Double getVolatility()        { return volatility; }
    public Double getMaxWinMultiplier()  { return maxWinMultiplier; }
    public Double getFreeSpinTriggerFreq() { return freeSpinTriggerFreq; }
    public Integer getLongestDryStreak() { return longestDryStreak; }
    public String getPrizeDistribution() { return prizeDistribution; }
    public String getConvergenceSample() { return convergenceSample; }
    public String getRtpBreakdown()      { return rtpBreakdown; }
    public Long getDurationMs()          { return durationMs; }
    public String getErrorMessage()      { return errorMessage; }
    public OffsetDateTime getStartedAt()    { return startedAt; }
    public OffsetDateTime getCompletedAt()  { return completedAt; }

    public void setStatus(final String status)               { this.status = status; }
    public void setRtpEmpirical(final Double v)              { this.rtpEmpirical = v; }
    public void setRtpStdError(final Double v)               { this.rtpStdError = v; }
    public void setRtpBaseGame(final Double v)               { this.rtpBaseGame = v; }
    public void setRtpFreeSpins(final Double v)              { this.rtpFreeSpins = v; }
    public void setHitFrequency(final Double v)              { this.hitFrequency = v; }
    public void setVolatility(final Double v)                { this.volatility = v; }
    public void setMaxWinMultiplier(final Double v)          { this.maxWinMultiplier = v; }
    public void setFreeSpinTriggerFreq(final Double v)       { this.freeSpinTriggerFreq = v; }
    public void setLongestDryStreak(final Integer v)         { this.longestDryStreak = v; }
    public void setPrizeDistribution(final String json)      { this.prizeDistribution = json; }
    public void setConvergenceSample(final String json)      { this.convergenceSample = json; }
    public void setRtpBreakdown(final String json)           { this.rtpBreakdown = json; }
    public void setDurationMs(final Long v)                  { this.durationMs = v; }
    public void setErrorMessage(final String v)              { this.errorMessage = v; }
    public void setCompletedAt(final OffsetDateTime v)       { this.completedAt = v; }
}
