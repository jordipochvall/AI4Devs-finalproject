package com.novacasino.application.math.exception;

/** The AI explainer is not configured (no ANTHROPIC_API_KEY). Maps to HTTP 503. */
public class ExplainerUnavailableException extends RuntimeException {
    public ExplainerUnavailableException() {
        super("AI explainer not available");
    }
}
