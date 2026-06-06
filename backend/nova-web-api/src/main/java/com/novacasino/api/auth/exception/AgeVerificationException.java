package com.novacasino.api.auth.exception;

/** The player is under 18 — 422 (DGOJ requirement). */
public class AgeVerificationException extends RuntimeException {
    public AgeVerificationException() {
        super("Insufficient age to register (18+ required)");
    }
}
