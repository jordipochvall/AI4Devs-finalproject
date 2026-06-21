package com.novacasino.api.math;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.math.dto.ExplanationDto;
import com.novacasino.api.math.dto.SimulationSummaryDto;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.SimulationExplanationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only history of simulations and their AI Q&A (HU-18), scoped to the operator of the token.
 */
@Service
public class SimulationHistoryService {

    private final SimulationRunJpaRepository simRepo;
    private final SimulationExplanationJpaRepository explanationRepo;

    public SimulationHistoryService(final SimulationRunJpaRepository simRepo,
                                    final SimulationExplanationJpaRepository explanationRepo) {
        this.simRepo = simRepo;
        this.explanationRepo = explanationRepo;
    }

    /** Operator's simulation history, newest first, optionally filtered by config or game (AC1/AC3). */
    @Transactional(readOnly = true)
    public PageResponse<SimulationSummaryDto> listSimulations(final Long operatorId, final Long configId,
                                                             final Long gameId, final Pageable pageable) {
        return PageResponse.of(simRepo.search(operatorId, configId, gameId, pageable), run ->
                new SimulationSummaryDto(run.getId(), run.getGameConfigId(), run.getStatus(),
                        run.getNumSpins(), run.getBetCents(), run.getRtpEmpirical(),
                        run.getStartedAt(), run.getCompletedAt()));
    }

    /** The AI Q&A thread of a simulation owned by the operator (AC2/AC3; 404 if missing/foreign). */
    @Transactional(readOnly = true)
    public List<ExplanationDto> listExplanations(final Long simulationId, final Long operatorId) {
        final SimulationRunEntity run = simRepo.findById(simulationId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
        return explanationRepo.findBySimulationRunIdOrderByAskedAtAsc(run.getId()).stream()
                .map(e -> new ExplanationDto(e.getQuestion(), e.getAnswer(), e.getModel(), e.getAskedAt()))
                .toList();
    }
}
