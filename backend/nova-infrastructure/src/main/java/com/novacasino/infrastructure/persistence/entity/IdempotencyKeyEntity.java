package com.novacasino.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Idempotency record for economic-effect operations (spin, recharge). A key is unique per
 * (user_id, endpoint, idem_key) and stores the original response to replay it on retries.
 */
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idem_key", nullable = false)
    private UUID idemKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 40)
    private String endpoint;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_status", nullable = false)
    private int responseStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_body", nullable = false, columnDefinition = "jsonb")
    private String responseBody;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected IdempotencyKeyEntity() { }

    /**
     * Builds a new idempotency record to be inserted.
     *
     * @param idemKey        client-supplied Idempotency-Key
     * @param userId         user who originated the request
     * @param endpoint       deduplicated operation ("spin", "recharge")
     * @param requestHash    SHA-256 of the request payload
     * @param responseStatus original HTTP status
     * @param responseBody   original response serialized as JSON
     */
    public IdempotencyKeyEntity(final UUID idemKey, final Long userId, final String endpoint,
                                final String requestHash, final int responseStatus, final String responseBody) {
        this.idemKey        = idemKey;
        this.userId         = userId;
        this.endpoint       = endpoint;
        this.requestHash    = requestHash;
        this.responseStatus = responseStatus;
        this.responseBody   = responseBody;
    }

    public Long getId()             { return id; }
    public UUID getIdemKey()        { return idemKey; }
    public Long getUserId()         { return userId; }
    public String getEndpoint()     { return endpoint; }
    public String getRequestHash()  { return requestHash; }
    public int getResponseStatus()  { return responseStatus; }
    public String getResponseBody() { return responseBody; }
}
