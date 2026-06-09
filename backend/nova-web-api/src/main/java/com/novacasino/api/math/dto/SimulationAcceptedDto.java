package com.novacasino.api.math.dto;

import java.time.OffsetDateTime;

/**
 * 202 Accepted body for a launched simulation (readme §4.4.4): the client polls {@code pollUrl}.
 *
 * @param simulationId id of the created run
 * @param status       always {@code RUNNING} at this point
 * @param startedAt    when the run was created
 * @param pollUrl      URL to poll for the status/result
 */
public record SimulationAcceptedDto(Long simulationId, String status, OffsetDateTime startedAt,
                                    String pollUrl) {
}
