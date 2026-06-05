package com.novacasino.api.auth.exception;

/** El jugador es menor de 18 años — 422 (requisito DGOJ). */
public class AgeVerificationException extends RuntimeException {
    public AgeVerificationException() {
        super("Edad insuficiente para registrarse (se requieren ≥18 años)");
    }
}
