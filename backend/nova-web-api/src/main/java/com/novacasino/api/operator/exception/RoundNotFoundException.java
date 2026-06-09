package com.novacasino.api.operator.exception;

/** The requested round does not exist (or is not the operator's). Maps to HTTP 404. */
public class RoundNotFoundException extends RuntimeException {
    public RoundNotFoundException(final Long roundId) {
        super("Round not found: " + roundId);
    }
}
