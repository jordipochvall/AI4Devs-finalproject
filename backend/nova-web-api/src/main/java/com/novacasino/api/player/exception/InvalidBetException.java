package com.novacasino.api.player.exception;

/**
 * The bet is outside the game's {@code [min,max]} range, not a multiple of the bet step, or not a
 * multiple of the number of paylines (readme §3.3.3). Maps to HTTP 422.
 */
public class InvalidBetException extends RuntimeException {
    public InvalidBetException() {
        super("Invalid bet");
    }
}
