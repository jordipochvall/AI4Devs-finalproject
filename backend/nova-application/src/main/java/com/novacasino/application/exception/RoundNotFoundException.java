package com.novacasino.application.exception;

/** The requested round does not exist or belongs to another operator (HU-3/HU-16). Maps to HTTP 404. */
public class RoundNotFoundException extends RuntimeException {
    public RoundNotFoundException(final Long roundId) {
        super("Round not found: " + roundId);
    }
}
