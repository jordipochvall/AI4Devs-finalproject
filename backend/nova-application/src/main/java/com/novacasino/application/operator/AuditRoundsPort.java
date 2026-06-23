package com.novacasino.application.operator;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.RoundSummaryDto;

import java.time.OffsetDateTime;

/** Output port for the operator's paginated, filterable round audit (HU-3). */
public interface AuditRoundsPort {

    PageResponse<RoundSummaryDto> search(Long operatorId, Long playerId, Long gameId,
                                         OffsetDateTime from, OffsetDateTime to, PageRequestDto page);
}
