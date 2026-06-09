package com.novacasino.api.math.exception;

/**
 * The simulation parameters are invalid: {@code numSpins} out of range (≤0 or &gt;10,000,000) or
 * {@code betCents} not a positive multiple of the payline count. Maps to HTTP 422.
 */
public class InvalidSimulationParamsException extends RuntimeException {
    public InvalidSimulationParamsException(final String detail) {
        super(detail);
    }
}
