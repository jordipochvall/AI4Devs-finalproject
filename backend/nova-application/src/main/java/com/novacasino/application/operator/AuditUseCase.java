package com.novacasino.application.operator;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.RoundSummaryDto;

import java.time.OffsetDateTime;

/** Operator audit search (HU-3): paginated, optional AND filters, newest first. Over {@link AuditRoundsPort}. */
public class AuditUseCase {

    private final AuditRoundsPort port;

    public AuditUseCase(final AuditRoundsPort port) {
        this.port = port;
    }

    public PageResponse<RoundSummaryDto> searchRounds(final Long operatorId, final Long playerId,
                                                      final Long gameId, final OffsetDateTime from,
                                                      final OffsetDateTime to, final PageRequestDto page) {
        return port.search(operatorId, playerId, gameId, from, to, page);
    }
}
