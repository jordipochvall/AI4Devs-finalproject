package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.IdempotencyKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data repository for idempotency records. */
public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    /** Finds an existing idempotency record for the (user, endpoint, key) triple. */
    Optional<IdempotencyKeyEntity> findByUserIdAndEndpointAndIdemKey(Long userId, String endpoint, UUID idemKey);
}
