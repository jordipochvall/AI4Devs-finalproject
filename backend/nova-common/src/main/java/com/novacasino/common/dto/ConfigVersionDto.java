package com.novacasino.common.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** A math version in a game's history (HU-17), flagged with whether it is the active one. */
public record ConfigVersionDto(
        Long id,
        int version,
        BigDecimal rtpTarget,
        BigDecimal volatilityTarget,
        boolean active,
        OffsetDateTime createdAt
) {
}
