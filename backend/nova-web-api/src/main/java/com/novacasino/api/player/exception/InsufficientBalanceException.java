package com.novacasino.api.player.exception;

/** The player's balance is lower than the bet. Maps to HTTP 422. */
public class InsufficientBalanceException extends RuntimeException {

    private final long neededCents;
    private final long availableCents;

    public InsufficientBalanceException(final long neededCents, final long availableCents) {
        super("Insufficient balance");
        this.neededCents = neededCents;
        this.availableCents = availableCents;
    }

    public long getNeededCents()    { return neededCents; }
    public long getAvailableCents() { return availableCents; }
}
