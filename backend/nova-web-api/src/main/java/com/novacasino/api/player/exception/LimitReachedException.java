package com.novacasino.api.player.exception;

/** A responsible-gaming loss limit has been reached; the spin is refused server-side (HU-19). 422. */
public class LimitReachedException extends RuntimeException {
    public LimitReachedException() {
        super("Responsible-gaming limit reached");
    }
}
