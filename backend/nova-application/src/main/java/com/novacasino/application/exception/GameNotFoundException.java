package com.novacasino.application.exception;

/** The requested game does not exist or is not active. Maps to HTTP 404. */
public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(final Long gameId) {
        super("Game not found: " + gameId);
    }
}
