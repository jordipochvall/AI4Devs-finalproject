package com.novacasino.api.player.dto;

import java.time.OffsetDateTime;

/** A player's responsible-gaming limit, including any pending (deferred) relaxation (HU-19). */
public record LimitDto(
        String limitType,
        String period,
        long amountCents,
        OffsetDateTime effectiveAt,
        Long pendingAmountCents,
        OffsetDateTime pendingEffectiveAt) {
}
