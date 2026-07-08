package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** Spring Data repository for operators. */
public interface OperatorJpaRepository extends JpaRepository<OperatorEntity, Long> {

    /** Finds an operator by its stable external code (e.g. "novacasino-default"). */
    Optional<OperatorEntity> findByCode(String code);

    /** Whether an operator with this code already exists (HU-25 onboarding). */
    boolean existsByCode(String code);

    /** All operators, oldest first (HU-25 admin listing). */
    List<OperatorEntity> findAllByOrderByIdAsc();

    /**
     * The DB-computed {@code created_at} (the column is {@code insertable=false}, so the entity held
     * in memory right after {@code save()} still has it {@code null}); a scalar query always hits the
     * DB rather than the persistence-context identity map, so this reliably returns the real value.
     */
    @Query("SELECT o.createdAt FROM OperatorEntity o WHERE o.id = :id")
    OffsetDateTime findCreatedAtById(@Param("id") Long id);
}
