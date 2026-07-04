package com.novacasino.application.math;

import com.novacasino.application.math.exception.SimulationNotFoundException;
import com.novacasino.common.dto.ExplanationDto;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.SimulationSummaryDto;

import java.util.List;

/** Read-only history of simulations and their AI Q&A (HU-18), over {@link SimulationQueryPort}. */
public class SimulationHistoryUseCase {

    private final SimulationQueryPort port;

    public SimulationHistoryUseCase(final SimulationQueryPort port) {
        this.port = port;
    }

    public PageResponse<SimulationSummaryDto> listSimulations(final Long operatorId, final Long configId,
                                                              final Long gameId, final PageRequestDto page) {
        return port.listSimulations(operatorId, configId, gameId, page);
    }

    public List<ExplanationDto> listExplanations(final Long simulationId, final Long operatorId) {
        return port.listExplanations(simulationId, operatorId)
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
    }
}
