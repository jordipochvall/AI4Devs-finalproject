package com.novacasino.application.admin.exception;

/** An operator with the given code already exists (HU-25). Maps to HTTP 409. */
public class OperatorCodeExistsException extends RuntimeException {
    public OperatorCodeExistsException(final String code) {
        super("Operator code already exists: " + code);
    }
}
