package com.novacasino.api.math;

import com.novacasino.api.math.dto.ExplanationDto;
import com.novacasino.api.math.exception.ExplainerUnavailableException;
import com.novacasino.api.math.exception.SimulationNotCompletedException;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import com.novacasino.infrastructure.persistence.entity.SimulationExplanationEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.SimulationExplanationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * AI explainability of simulation results (HU-8): composes a prompt from the simulation metrics,
 * asks the {@link Explainer} (Claude) and persists the Q&A for traceability. The explainer is
 * optional — if no API key is configured the bean is absent and the endpoint reports 503 (AC4),
 * while everything else keeps working. The platform never declares the target RTP; the AI only
 * interprets the measured metrics.
 */
@Service
public class ExplainService {

    private final SimulationRunJpaRepository simRepo;
    private final SimulationExplanationJpaRepository explanationRepo;
    private final Optional<Explainer> explainer;

    public ExplainService(final SimulationRunJpaRepository simRepo,
                          final SimulationExplanationJpaRepository explanationRepo,
                          final Optional<Explainer> explainer) {
        this.simRepo = simRepo;
        this.explanationRepo = explanationRepo;
        this.explainer = explainer;
    }

    /**
     * Answers a question about a COMPLETED simulation and records the Q&A.
     *
     * @param simulationId target simulation
     * @param operatorId   operator scope (from the token)
     * @param userId       analyst asking
     * @param question     natural-language question
     * @return the answer with the model used and the timestamp
     */
    @Transactional
    public ExplanationDto explain(final Long simulationId, final Long operatorId,
                                  final Long userId, final String question) {
        final SimulationRunEntity run = simRepo.findById(simulationId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
        if (!SimulationRunEntity.COMPLETED.equals(run.getStatus())) {
            throw new SimulationNotCompletedException(simulationId);   // AC2
        }
        final Explainer ai = explainer.orElseThrow(ExplainerUnavailableException::new); // AC4

        final Explanation explanation = ai.explain(buildPrompt(run, question));

        final SimulationExplanationEntity saved = explanationRepo.save(new SimulationExplanationEntity(
                simulationId, userId, question, explanation.answer(), explanation.model()));
        return new ExplanationDto(question, saved.getAnswer(), saved.getModel(), OffsetDateTime.now());
    }

    /** Composes the prompt from the simulation's measured metrics plus the analyst's question. */
    private String buildPrompt(final SimulationRunEntity r, final String question) {
        return """
                You are assisting a slot-game mathematician. Interpret the EMPIRICAL metrics of a Monte
                Carlo simulation (you do not set targets; you only explain the measurements). Answer
                concisely in the language of the question.

                Simulation metrics:
                - spins: %d, betCents: %d
                - RTP empirical: %s (std error: %s)
                - RTP base game: %s, RTP free spins: %s
                - hit frequency: %s, volatility: %s
                - max win multiplier: %s, free-spins trigger freq: %s
                - longest dry streak: %s

                Question: %s
                """.formatted(
                r.getNumSpins(), r.getBetCents(),
                r.getRtpEmpirical(), r.getRtpStdError(),
                r.getRtpBaseGame(), r.getRtpFreeSpins(),
                r.getHitFrequency(), r.getVolatility(),
                r.getMaxWinMultiplier(), r.getFreeSpinTriggerFreq(),
                r.getLongestDryStreak(), question);
    }
}
