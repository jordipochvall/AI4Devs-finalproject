package com.novacasino.application.math.exception;

/** Config version not found — 404. */
public class ConfigNotFoundException extends RuntimeException {
    public ConfigNotFoundException(final Long configId) {
        super("Config version not found: " + configId);
    }
}
