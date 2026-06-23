package com.novacasino.application.operator;

import com.novacasino.common.dto.DashboardDto;
import com.novacasino.common.dto.RoundDetailDto;

import java.time.OffsetDateTime;
import java.util.Optional;

/** Output port for the operator activity dashboard and single-round detail (HU-16). */
public interface OperatorDashboardPort {

    /** Aggregated KPIs over [from, to] (null bounds default to a wide window in the adapter). */
    DashboardDto dashboard(Long operatorId, OffsetDateTime from, OffsetDateTime to);

    /** A single round's detail; empty if missing or owned by another operator. */
    Optional<RoundDetailDto> roundDetail(Long roundId, Long operatorId);
}
