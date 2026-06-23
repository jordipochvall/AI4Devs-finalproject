package com.novacasino.application.operator.exception;

/** A commercial-config update is incoherent (bounds or payline multiples) (HU-15). Maps to 422. */
public class InvalidCommercialConfigException extends RuntimeException {
    public InvalidCommercialConfigException(final String message) {
        super(message);
    }
}
