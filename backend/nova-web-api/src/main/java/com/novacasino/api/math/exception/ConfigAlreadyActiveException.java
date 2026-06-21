package com.novacasino.api.math.exception;

/** Attempt to publish the math version that is already active for a game (HU-17). Maps to HTTP 409. */
public class ConfigAlreadyActiveException extends RuntimeException {
    public ConfigAlreadyActiveException(final Long configId) {
        super("Config " + configId + " is already the active version");
    }
}
