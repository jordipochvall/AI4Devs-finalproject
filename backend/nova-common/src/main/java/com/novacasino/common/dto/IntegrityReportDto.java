package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/**
 * Result of verifying the audit integrity chain over a window (HU-20). Shared DTO in {@code
 * nova-common} so the application use case produces it and the web layer returns it without coupling.
 *
 * @param from               window start (inclusive)
 * @param to                 window end (inclusive)
 * @param checked            number of rounds verified
 * @param consistent         true if the chain is intact
 * @param firstBrokenRoundId the first round whose chain breaks (null when consistent)
 */
public record IntegrityReportDto(
        OffsetDateTime from,
        OffsetDateTime to,
        long checked,
        boolean consistent,
        Long firstBrokenRoundId) {
}
