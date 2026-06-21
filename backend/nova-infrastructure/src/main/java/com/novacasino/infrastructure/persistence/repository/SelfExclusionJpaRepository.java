package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.SelfExclusionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;

/** Self-exclusion periods per player (HU-19). */
public interface SelfExclusionJpaRepository extends JpaRepository<SelfExclusionEntity, Long> {

    /** Whether the player has a self-exclusion still in force at the given instant. */
    boolean existsByUserIdAndEndAtAfter(Long userId, OffsetDateTime instant);
}
