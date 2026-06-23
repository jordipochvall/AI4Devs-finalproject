package com.novacasino.application.operator.exception;

/** The target player does not exist within the operator (HU-6). Maps to HTTP 404. */
public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(final Long playerId) {
        super("Player not found: " + playerId);
    }
}
