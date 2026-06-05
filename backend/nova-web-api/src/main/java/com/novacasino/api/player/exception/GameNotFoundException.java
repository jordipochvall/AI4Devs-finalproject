package com.novacasino.api.player.exception;

/** Juego inexistente o inactivo — 404. */
public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(Long gameId) {
        super("Juego no encontrado o inactivo: " + gameId);
    }
}
