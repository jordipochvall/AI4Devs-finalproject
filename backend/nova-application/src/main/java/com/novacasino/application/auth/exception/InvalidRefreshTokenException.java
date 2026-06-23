package com.novacasino.application.auth.exception;

/** The presented refresh token is unknown, expired or revoked (HU-13). Maps to HTTP 401. */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() {
        super("Invalid refresh token");
    }
}
