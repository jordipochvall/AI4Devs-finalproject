package com.novacasino.api.math.dto;

import java.time.OffsetDateTime;

/**
 * One simulation in the operator's history (HU-18). Summary only — the full metrics are served by
 * {@code GET /math/simulations/{id}}.
 *
 * @param id           simulation run id
 * @param gameConfigId math version simulated
 * @param status       RUNNING / COMPLETED / FAILED
 * @param numSpins     spins requested
 * @param betCents     bet per spin
 * @param rtpEmpirical empirical RTP (null until completed)
 * @param startedAt    when the run started
 * @param completedAt  when it finished (null while running)
 */
public record SimulationSummaryDto(
        Long id,
        Long gameConfigId,
        String status,
        long numSpins,
        long betCents,
        Double rtpEmpirical,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt) {
}
