package com.novacasino.api.player.dto;

/** Saldo virtual del jugador. */
public record WalletDto(
        long   balanceCents,
        String currency
) {}
