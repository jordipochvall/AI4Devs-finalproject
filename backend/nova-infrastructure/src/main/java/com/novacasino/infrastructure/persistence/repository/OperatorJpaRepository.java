package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data repository for operators. */
public interface OperatorJpaRepository extends JpaRepository<OperatorEntity, Long> {

    /** Finds an operator by its stable external code (e.g. "novacasino-default"). */
    Optional<OperatorEntity> findByCode(String code);
}
