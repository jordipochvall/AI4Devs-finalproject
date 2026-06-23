package com.novacasino.common.dto;

/** Player's virtual balance. */
public record WalletDto(
        long   balanceCents,
        String currency
) {}
