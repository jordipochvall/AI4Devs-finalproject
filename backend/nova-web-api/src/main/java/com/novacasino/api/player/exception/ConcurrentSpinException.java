package com.novacasino.api.player.exception;

/**
 * The wallet's optimistic lock kept conflicting after several retries (concurrent balance change).
 * Maps to HTTP 409 — distinct from the idempotency-key conflict.
 */
public class ConcurrentSpinException extends RuntimeException {
    public ConcurrentSpinException() {
        super("Concurrent wallet modification");
    }
}
