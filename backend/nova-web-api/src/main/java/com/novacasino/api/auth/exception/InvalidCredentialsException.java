package com.novacasino.api.auth.exception;

/** Invalid credentials on login — 401. */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
