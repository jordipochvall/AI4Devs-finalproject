package com.novacasino.api.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.novacasino.infrastructure.persistence.repository.IdempotencyKeyJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Reusable idempotency mechanism for economic-effect operations (recharge in HU-6;
 * spin in HU-1-BE-02). Guarantees that a retry with the same {@code Idempotency-Key}
 * does not re-run the operation: it returns the original response.
 *
 * <p>The whole operation ({@code operation.get()}) and the key insertion happen in a
 * single transaction: if anything fails, neither the effect nor the key remains.
 */
@Service
public class IdempotencyService {

    private final IdempotencyKeyJpaRepository repo;
    private final ObjectMapper objectMapper;

    public IdempotencyService(final IdempotencyKeyJpaRepository repo, final ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    /**
     * Runs {@code operation} idempotently.
     *
     * @param userId         user originating the request (part of the unique key)
     * @param endpoint       deduplicated operation (e.g. "recharge", "spin")
     * @param idemKey        value of the Idempotency-Key header
     * @param requestForHash object whose JSON defines the payload (to detect key reuse with different data)
     * @param responseType   response type (to replay it from the stored JSON)
     * @param operation      the side-effecting operation to run exactly once
     * @return the operation result, or the replayed original response
     */
    @Transactional
    public <T> T execute(final Long userId, final String endpoint, final UUID idemKey,
                         final Object requestForHash, final Class<T> responseType,
                         final Supplier<T> operation) {
        final String hash = sha256Hex(toJson(requestForHash));

        final Optional<IdempotencyKeyEntity> existing =
                repo.findByUserIdAndEndpointAndIdemKey(userId, endpoint, idemKey);
        if (existing.isPresent()) {
            final IdempotencyKeyEntity e = existing.get();
            if (!e.getRequestHash().equals(hash)) {
                throw new IdempotencyConflictException();
            }
            // Replay the EXACT original response that was stored when the operation first ran — a
            // point-in-time snapshot. Any embedded figures (e.g. a spin's balancePost) are those of the
            // original execution; they are intentionally NOT refreshed, so a replay can show a balance
            // that has since changed. This is the contract of idempotency: the same request returns the
            // same response, never a second effect. Clients needing the live balance read it separately.
            return fromJson(e.getResponseBody(), responseType);
        }

        final T response = operation.get();
        repo.save(new IdempotencyKeyEntity(idemKey, userId, endpoint, hash, 200, toJson(response)));
        return response;
    }

    // -------------------------------------------------------------------------

    private String toJson(final Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize to JSON", e);
        }
    }

    private <T> T fromJson(final String json, final Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not deserialize the idempotent response", e);
        }
    }

    private String sha256Hex(final String input) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(64);
            for (final byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
