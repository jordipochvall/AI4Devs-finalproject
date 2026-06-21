package com.novacasino.api.operator.dto;

import com.novacasino.api.player.dto.WinningPaylineDto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Detail of a single round for the operator (HU-16), without the full replay reconstruction: amounts
 * plus the resulting symbol grid and winning paylines, read from the immutable {@code game_rounds} row.
 */
public record RoundDetailDto(
        Long roundId,
        Long gameId,
        Long playerId,
        Long gameConfigId,
        long betCents,
        long winCents,
        long balancePreCents,
        long balancePostCents,
        boolean freeSpin,
        OffsetDateTime createdAt,
        List<List<String>> view,
        List<WinningPaylineDto> winningPaylines,
        int scatterCount
) {
}
