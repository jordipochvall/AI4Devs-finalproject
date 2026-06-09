package com.novacasino.api.math.exception;

/** The simulation must be COMPLETED before it can be explained. Maps to HTTP 422. */
public class SimulationNotCompletedException extends RuntimeException {
    public SimulationNotCompletedException(final Long simulationId) {
        super("Simulation not completed: " + simulationId);
    }
}
