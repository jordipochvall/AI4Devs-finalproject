package com.novacasino.common.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Operator activity dashboard (HU-16): aggregated KPIs over a date window, scoped to the operator.
 *
 * @param from           start of the window (inclusive)
 * @param to             end of the window (inclusive)
 * @param activePlayers  distinct players with at least one round in the window
 * @param ggrCents       gross gaming revenue in cents (wagered - paid out)
 * @param totalRounds    total rounds (base + free spins)
 * @param topGames       most-played games, descending by round count
 */
public record DashboardDto(
        OffsetDateTime from,
        OffsetDateTime to,
        long activePlayers,
        long ggrCents,
        long totalRounds,
        List<TopGameDto> topGames
) {
    /** A most-played game entry. */
    public record TopGameDto(Long gameId, String name, String theme, long rounds) {
    }
}
