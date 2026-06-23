package com.novacasino.application.math;

import com.novacasino.application.math.SimulationQueryPort.SimulationView;
import com.novacasino.application.math.exception.ExplainerUnavailableException;
import com.novacasino.application.math.exception.SimulationNotCompletedException;
import com.novacasino.application.math.exception.SimulationNotFoundException;
import com.novacasino.common.dto.ExplanationDto;
import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import jakarta.transaction.Transactional;

import java.util.Optional;

/**
 * AI explainability of simulation results (HU-8): composes a prompt from the simulation's empirical
 * metrics, asks the {@link Explainer} and persists the Q&A. The explainer is optional — if no API key
 * is configured the bean is absent and the endpoint reports 503 (AC4). The platform never declares
 * the target RTP; the AI only interprets the measured metrics.
 */
public class ExplainUseCase {

    private static final String COMPLETED = "COMPLETED";

    private final SimulationQueryPort port;
    private final Optional<Explainer> explainer;

    public ExplainUseCase(final SimulationQueryPort port, final Optional<Explainer> explainer) {
        this.port = port;
        this.explainer = explainer;
    }

    /** Answers a question about a COMPLETED simulation and records the Q&A. */
    @Transactional
    public ExplanationDto explain(final Long simulationId, final Long operatorId,
                                  final Long userId, final String question) {
        final SimulationView view = port.findForExplain(simulationId, operatorId)
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
        if (!COMPLETED.equals(view.status())) {
            throw new SimulationNotCompletedException(simulationId);   // AC2
        }
        final Explainer ai = explainer.orElseThrow(ExplainerUnavailableException::new); // AC4

        final Explanation explanation = ai.explain(buildPrompt(view, question));
        return port.saveExplanation(simulationId, userId, question, explanation.answer(), explanation.model());
    }

    /** Composes the prompt from the simulation's measured metrics plus the analyst's question. */
    private String buildPrompt(final SimulationView r, final String question) {
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
                r.numSpins(), r.betCents(),
                r.rtpEmpirical(), r.rtpStdError(),
                r.rtpBaseGame(), r.rtpFreeSpins(),
                r.hitFrequency(), r.volatility(),
                r.maxWinMultiplier(), r.freeSpinTriggerFreq(),
                r.longestDryStreak(), question);
    }
}
