package com.novacasino.application.operator;

import java.util.List;

/** Command carrying the operator's commercial-config update (HU-15). */
public record GameCommercialUpdate(
        long minBetCents,
        long maxBetCents,
        long betStepCents,
        boolean active,
        List<String> allowedCurrencies) {
}
