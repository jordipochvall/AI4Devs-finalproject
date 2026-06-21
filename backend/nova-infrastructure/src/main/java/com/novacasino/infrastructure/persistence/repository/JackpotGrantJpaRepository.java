package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.JackpotGrantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Append-only jackpot grant history (HU-26). */
public interface JackpotGrantJpaRepository extends JpaRepository<JackpotGrantEntity, Long> {
}
