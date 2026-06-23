package com.novacasino.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;

/**
 * Status/result of a simulation for polling (readme §4.4.4). While {@code RUNNING} the metric fields
 * are {@code null}; once {@code COMPLETED} they are populated. {@code FAILED} carries an
 * {@code errorMessage}.
 */
public record SimulationStatusDto(
        Long simulationId,
        String status,
        long numSpins,
        long betCents,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        Long durationMs,
        Double rtpEmpirical,
        Double rtpStdError,
        Double rtpBaseGame,
        Double rtpFreeSpins,
        Double hitFrequency,
        Double volatility,
        Double maxWinMultiplier,
        Double freeSpinTriggerFreq,
        Integer longestDryStreak,
        JsonNode prizeDistribution,
        JsonNode convergenceSample,
        JsonNode rtpBreakdown,
        String errorMessage) {
}
