package com.novacasino.api.player.exception;

/** Game not found or inactive — 404. */
public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(final Long gameId) {
        super("Game not found or inactive: " + gameId);
    }
}
