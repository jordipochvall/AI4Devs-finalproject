package com.novacasino.infrastructure.persistence.projection;

/** Aggregation row for the operator dashboard's "most-played games" (HU-16). */
public interface TopGameProjection {
    Long getGameId();
    long getRounds();
}
