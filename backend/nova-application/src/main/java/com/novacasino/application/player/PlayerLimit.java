package com.novacasino.application.player;

import java.time.OffsetDateTime;

/** Application view of a responsible-gaming limit (HU-19); the adapter maps the JPA row into this. */
public record PlayerLimit(
        Long id,
        Long userId,
        String limitType,
        String period,
        long amountCents,
        OffsetDateTime effectiveAt,
        Long pendingAmountCents,
        OffsetDateTime pendingEffectiveAt) {
}
