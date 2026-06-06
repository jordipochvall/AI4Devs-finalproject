package com.novacasino.api.operator.exception;

/** Non-positive recharge amount — 422. */
public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException() {
        super("The recharge amount must be positive");
    }
}
