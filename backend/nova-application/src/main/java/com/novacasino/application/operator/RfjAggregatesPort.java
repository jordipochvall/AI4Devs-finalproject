package com.novacasino.application.operator;

import java.time.OffsetDateTime;

/** Output port for the RFJ report's monthly round aggregates (HU-21). */
public interface RfjAggregatesPort {

    /** Aggregated {@code game_rounds} figures for the operator over [from, to). */
    RfjAggregates aggregate(Long operatorId, OffsetDateTime from, OffsetDateTime to);

    /** Monthly aggregates used to build the RFJ document. */
    record RfjAggregates(long totalWageredCents, long totalWonCents, long totalRounds, long activePlayers) {
    }
}
