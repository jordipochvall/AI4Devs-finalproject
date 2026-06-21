package com.novacasino.api.auth.exception;

/** The presented refresh token is unknown, expired or revoked. Maps to HTTP 401. */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Invalid refresh token");
    }
}
