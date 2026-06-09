package com.novacasino.api.operator.dto;

import java.time.OffsetDateTime;

/**
 * One audited game round in the operator's audit listing (readme §3.2.8). Summary only — the full
 * visual replay is served by {@code GET /operator/rounds/{id}/replay} (HU-3-BE-02).
 *
 * @param id                 round id
 * @param playerId           player who span
 * @param gameId             game played
 * @param gameConfigId       math version used
 * @param betCents           bet of the round (0 for free-spin children)
 * @param winCents           win of this round
 * @param balancePostCents   balance after the round
 * @param freeSpin           whether this is a free-spin child round
 * @param triggeringRoundId  base round that triggered it (free-spin children only)
 * @param createdAt          when the round was recorded
 */
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
