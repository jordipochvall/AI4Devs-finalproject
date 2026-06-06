package com.novacasino.api.operator.exception;

/** Player (or their wallet) not found — 404. */
public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(final Long playerId) {
        super("Player not found: " + playerId);
    }
}
