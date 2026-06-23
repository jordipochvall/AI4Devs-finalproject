package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/** One audited game round in the operator's audit listing (readme §3.2.8). Summary only. */
public record RoundSummaryDto(
        Long id,
        Long playerId,
        Long gameId,
        Long gameConfigId,
        long betCents,
        long winCents,
        long balancePostCents,
        boolean freeSpin,
        Long triggeringRoundId,
        OffsetDateTime createdAt) {
}
