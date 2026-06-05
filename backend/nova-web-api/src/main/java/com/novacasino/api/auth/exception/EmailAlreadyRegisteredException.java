package com.novacasino.api.auth.exception;

/** El email ya existe en el operador — 409. */
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String email) {
        super("Email ya registrado: " + email);
    }
}
