package com.novacasino.application.math.exception;

/** The requested simulation does not exist (or is not the operator's). Maps to HTTP 404. */
public class SimulationNotFoundException extends RuntimeException {
    public SimulationNotFoundException(final Long simulationId) {
        super("Simulation not found: " + simulationId);
    }
}
