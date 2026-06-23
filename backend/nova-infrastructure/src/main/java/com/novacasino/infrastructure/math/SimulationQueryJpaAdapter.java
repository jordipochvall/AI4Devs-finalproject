package com.novacasino.infrastructure.math;

import com.novacasino.application.math.SimulationQueryPort;
import com.novacasino.common.dto.ExplanationDto;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.SimulationSummaryDto;
import com.novacasino.infrastructure.persistence.entity.SimulationExplanationEntity;
import com.novacasino.infrastructure.persistence.repository.SimulationExplanationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import com.novacasino.infrastructure.support.Pages;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/** JPA adapter for {@link SimulationQueryPort} (HU-18 / HU-8): history reads + Q&A persistence. */
@Component
public class SimulationQueryJpaAdapter implements SimulationQueryPort {

    private final SimulationRunJpaRepository simRepo;
    private final SimulationExplanationJpaRepository explanationRepo;

    public SimulationQueryJpaAdapter(final SimulationRunJpaRepository simRepo,
                                     final SimulationExplanationJpaRepository explanationRepo) {
        this.simRepo = simRepo;
        this.explanationRepo = explanationRepo;
    }

    @Override
    public PageResponse<SimulationSummaryDto> listSimulations(final Long operatorId, final Long configId,
                                                              final Long gameId, final PageRequestDto page) {
        return Pages.of(simRepo.search(operatorId, configId, gameId, PageRequest.of(page.page(), page.size())),
                run -> new SimulationSummaryDto(run.getId(), run.getGameConfigId(), run.getStatus(),
                        run.getNumSpins(), run.getBetCents(), run.getRtpEmpirical(),
                        run.getStartedAt(), run.getCompletedAt()));
    }

    @Override
    public Optional<List<ExplanationDto>> listExplanations(final Long simulationId, final Long operatorId) {
        return simRepo.findById(simulationId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .map(run -> explanationRepo.findBySimulationRunIdOrderByAskedAtAsc(run.getId()).stream()
                        .map(e -> new ExplanationDto(e.getQuestion(), e.getAnswer(), e.getModel(), e.getAskedAt()))
                        .toList());
    }

    @Override
    public Optional<SimulationView> findForExplain(final Long simulationId, final Long operatorId) {
        return simRepo.findById(simulationId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .map(r -> new SimulationView(r.getStatus(), r.getNumSpins(), r.getBetCents(),
                        r.getRtpEmpirical(), r.getRtpStdError(), r.getRtpBaseGame(), r.getRtpFreeSpins(),
                        r.getHitFrequency(), r.getVolatility(), r.getMaxWinMultiplier(),
                        r.getFreeSpinTriggerFreq(), r.getLongestDryStreak()));
    }

    @Override
    public ExplanationDto saveExplanation(final Long simulationId, final Long userId, final String question,
                                          final String answer, final String model) {
        final SimulationExplanationEntity saved = explanationRepo.save(
                new SimulationExplanationEntity(simulationId, userId, question, answer, model));
        return new ExplanationDto(question, saved.getAnswer(), saved.getModel(), OffsetDateTime.now());
    }
}
