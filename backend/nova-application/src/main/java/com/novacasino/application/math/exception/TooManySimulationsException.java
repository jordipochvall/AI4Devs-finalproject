package com.novacasino.application.math.exception;

/**
 * Too many simulations are already {@code RUNNING} (HU-37): the demo VPS has bounded CPU/memory, so
 * new launches are rejected instead of queued without limit. Maps to HTTP 429.
 */
public class TooManySimulationsException extends RuntimeException {
    public TooManySimulationsException(final String detail) {
        super(detail);
    }
}
