package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for mass-simulation runs. */
public interface SimulationRunJpaRepository extends JpaRepository<SimulationRunEntity, Long> {
}
