package com.novacasino.api.player.exception;

/** The player is within an active self-exclusion period; play is blocked server-side (HU-19). 403. */
public class SelfExcludedException extends RuntimeException {
    public SelfExcludedException() {
        super("Player is self-excluded");
    }
}
