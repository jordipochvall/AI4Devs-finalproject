package com.novacasino.application.math;

import com.novacasino.common.dto.ExplanationDto;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.SimulationSummaryDto;

import java.util.List;
import java.util.Optional;

/** Output port for simulation history and AI Q&A persistence (HU-18 / HU-8). */
public interface SimulationQueryPort {

    PageResponse<SimulationSummaryDto> listSimulations(Long operatorId, Long configId, Long gameId,
                                                       PageRequestDto page);

    /** The AI Q&A thread of a simulation; empty if missing or owned by another operator. */
    Optional<List<ExplanationDto>> listExplanations(Long simulationId, Long operatorId);

    /** A simulation's status + empirical metrics for explanation; empty if missing/foreign. */
    Optional<SimulationView> findForExplain(Long simulationId, Long operatorId);

    /** Persists an AI Q&A and returns it (timestamped now). */
    ExplanationDto saveExplanation(Long simulationId, Long userId, String question, String answer, String model);

    /** A completed simulation's empirical metrics, used to compose the AI prompt. */
    record SimulationView(
            String status,
            long numSpins,
            long betCents,
            Double rtpEmpirical,
            Double rtpStdError,
            Double rtpBaseGame,
            Double rtpFreeSpins,
            Double hitFrequency,
            Double volatility,
            Double maxWinMultiplier,
            Double freeSpinTriggerFreq,
            Integer longestDryStreak) {
    }
}
