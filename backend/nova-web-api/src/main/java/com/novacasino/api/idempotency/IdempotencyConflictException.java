package com.novacasino.api.idempotency;

/** Same Idempotency-Key reused with a different payload — 409. */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
        super("Idempotency-Key reused with a different payload");
    }
}
