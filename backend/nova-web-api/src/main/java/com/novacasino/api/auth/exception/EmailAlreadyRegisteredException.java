package com.novacasino.api.auth.exception;

/** Email already exists within the operator — 409. */
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(final String email) {
        super("Email already registered: " + email);
    }
}
