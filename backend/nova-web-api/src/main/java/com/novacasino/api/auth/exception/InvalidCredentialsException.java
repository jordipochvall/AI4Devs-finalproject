package com.novacasino.api.auth.exception;

/** Credenciales inválidas en login — 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
}
