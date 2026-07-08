package com.novacasino.application.math;

import com.novacasino.application.math.exception.ConfigNotFoundException;
import com.novacasino.application.math.exception.InvalidSimulationParamsException;
import com.novacasino.application.math.exception.SimulationNotFoundException;
import com.novacasino.application.math.exception.TooManySimulationsException;
import com.novacasino.common.dto.SimulationAcceptedDto;
import com.novacasino.common.dto.SimulationStatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launches mass simulations and exposes their status (HU-2). It validates the parameters (numSpins
 * range and betCents as a positive multiple of the payline count) before recording a RUNNING run and
 * triggering its asynchronous execution. The platform measures; it never declares the target RTP.
 */
public class SimulationUseCase {

    private static final Logger log = LoggerFactory.getLogger(SimulationUseCase.class);

    /** Hard cap on simulated spins (readme §4.4.4). */
    static final long MAX_SPINS = 10_000_000L;

    private final SimulationLaunchPort port;
    private final int maxConcurrentSimulations;

    public SimulationUseCase(final SimulationLaunchPort port, final int maxConcurrentSimulations) {
        this.port = port;
        this.maxConcurrentSimulations = maxConcurrentSimulations;
    }

    /**
     * Validates the parameters, records a RUNNING run and triggers its asynchronous execution.
     * Not transactional: the RUNNING row is committed before the async worker reads it.
     */
    public SimulationAcceptedDto launch(final Long operatorId, final Long userId, final Long configId,
                                        final Long numSpins, final Long betCents) {
        if (numSpins == null || numSpins <= 0 || numSpins > MAX_SPINS) {
            throw new InvalidSimulationParamsException("numSpins must be between 1 and " + MAX_SPINS);
        }
        // HU-37: reject before touching config/payline lookups if the demo VPS is already at capacity.
        if (port.countRunning() >= maxConcurrentSimulations) {
            throw new TooManySimulationsException(
                    "Too many simulations running (max " + maxConcurrentSimulations + ")");
        }
        final int paylineCount = port.ownedConfigPaylineCount(configId, operatorId)
                .orElseThrow(() -> new ConfigNotFoundException(configId));
        if (betCents == null || betCents <= 0 || betCents % paylineCount != 0) {
            throw new InvalidSimulationParamsException("betCents must be a positive multiple of the payline count");
        }
        final SimulationAcceptedDto accepted = port.createAndLaunch(operatorId, userId, configId, numSpins, betCents);
        log.info("Simulation launched: id={}, operatorId={}, configId={}, numSpins={}, betCents={}, by userId={}",
                accepted.simulationId(), operatorId, configId, numSpins, betCents, userId);
        return accepted;
    }

    /** Returns the status (and metrics if completed) of a simulation owned by the operator. */
    public SimulationStatusDto getSimulation(final Long simulationId, final Long operatorId) {
        return port.getSimulation(simulationId, operatorId)
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
    }
}
