package com.novacasino.api.auth.exception;

/** The user's account or its operator is deactivated; login/operation is refused (HU-25). 403. */
public class OperatorInactiveException extends RuntimeException {
    public OperatorInactiveException() {
        super("Operator or user is inactive");
    }
}
