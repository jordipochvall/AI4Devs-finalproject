package com.novacasino.api.player.dto;

/** Player's virtual balance. */
public record WalletDto(
        long   balanceCents,
        String currency
) {}
