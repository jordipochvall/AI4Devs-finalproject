package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Immutable version of a game's math. A new row is inserted on every save in the editor;
 * existing rows are never modified (a DB trigger forbids UPDATE/DELETE).
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

    /** JSONB column; read as raw JSON text and parsed to a JSON tree by the service. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String config;

    @Column(name = "rtp_target", nullable = false)
    private BigDecimal rtpTarget;

    @Column(name = "volatility_target")
    private BigDecimal volatilityTarget;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column
    private String notes;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected GameConfigEntity() { }

    /**
     * Builds a new math version to be inserted.
     *
     * @param gameId           owning game
     * @param version          monotonically increasing version number
     * @param config           validated config JSON (raw text)
     * @param rtpTarget        target RTP declared by the mathematician (not computed)
     * @param volatilityTarget target volatility (optional)
     * @param createdByUserId  math analyst who created the version
     * @param notes            optional free-text note
     */
    public GameConfigEntity(final Long gameId, final int version, final String config,
                            final BigDecimal rtpTarget, final BigDecimal volatilityTarget,
                            final Long createdByUserId, final String notes) {
        this.gameId           = gameId;
        this.version          = version;
        this.config           = config;
        this.rtpTarget        = rtpTarget;
        this.volatilityTarget = volatilityTarget;
        this.createdByUserId  = createdByUserId;
        this.notes            = notes;
    }

    public Long getId()                     { return id; }
    public Long getGameId()                 { return gameId; }
    public int getVersion()                 { return version; }
    public String getConfig()               { return config; }
    public BigDecimal getRtpTarget()        { return rtpTarget; }
    public BigDecimal getVolatilityTarget() { return volatilityTarget; }
    public Long getCreatedByUserId()        { return createdByUserId; }
    public String getNotes()                { return notes; }
    public OffsetDateTime getCreatedAt()    { return createdAt; }
}
