package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.SimulationExplanationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data repository for the AI Q&A history of simulations. */
public interface SimulationExplanationJpaRepository
        extends JpaRepository<SimulationExplanationEntity, Long> {

    /** The AI Q&A thread of a simulation, chronological (HU-18). */
    List<SimulationExplanationEntity> findBySimulationRunIdOrderByAskedAtAsc(Long simulationRunId);
}
