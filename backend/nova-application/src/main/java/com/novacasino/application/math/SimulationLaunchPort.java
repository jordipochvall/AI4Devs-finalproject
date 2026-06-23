package com.novacasino.application.math;

import com.novacasino.common.dto.SimulationAcceptedDto;
import com.novacasino.common.dto.SimulationStatusDto;

import java.util.Optional;

/** Output port to launch mass simulations and poll their status (HU-2). */
public interface SimulationLaunchPort {

    /** Payline count of a config owned by the operator (via its game); empty if missing/foreign. */
    Optional<Integer> ownedConfigPaylineCount(Long configId, Long operatorId);

    /** Persists a RUNNING run and triggers its asynchronous execution, returning the 202 body. */
    SimulationAcceptedDto createAndLaunch(Long operatorId, Long userId, Long configId,
                                          long numSpins, long betCents);

    /** Status (and metrics if completed) of a simulation owned by the operator; empty if missing/foreign. */
    Optional<SimulationStatusDto> getSimulation(Long simulationId, Long operatorId);
}
