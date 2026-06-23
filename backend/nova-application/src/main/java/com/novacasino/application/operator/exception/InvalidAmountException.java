package com.novacasino.application.operator.exception;

/** A recharge amount is null or non-positive (HU-6). Maps to HTTP 422. */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("Amount must be positive");
    }
}
