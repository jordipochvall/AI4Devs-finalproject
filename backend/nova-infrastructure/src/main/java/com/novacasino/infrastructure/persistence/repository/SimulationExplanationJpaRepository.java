package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.SimulationExplanationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for the AI Q&A history of simulations. */
public interface SimulationExplanationJpaRepository
        extends JpaRepository<SimulationExplanationEntity, Long> {
}
