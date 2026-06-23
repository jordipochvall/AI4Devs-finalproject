package com.novacasino.application.admin.exception;

/** The requested operator does not exist (HU-25). Maps to HTTP 404. */
public class OperatorNotFoundException extends RuntimeException {
    public OperatorNotFoundException(final Long id) {
        super("Operator not found: " + id);
    }
}
