package com.novacasino.api.operator.exception;

/**
 * The submitted commercial configuration is inconsistent (e.g. bet bounds out of order, or the
 * minimum bet / step is not a multiple of the active config's payline count, HU-15). Maps to 422.
 */
public class InvalidCommercialConfigException extends RuntimeException {
    public InvalidCommercialConfigException(final String message) {
        super(message);
    }
}
