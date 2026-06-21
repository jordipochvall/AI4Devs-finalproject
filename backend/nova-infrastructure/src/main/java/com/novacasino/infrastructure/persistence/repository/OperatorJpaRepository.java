package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
