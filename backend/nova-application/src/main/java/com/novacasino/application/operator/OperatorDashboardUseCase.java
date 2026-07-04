package com.novacasino.application.operator;

import com.novacasino.application.exception.RoundNotFoundException;
import com.novacasino.common.dto.DashboardDto;
import com.novacasino.common.dto.RoundDetailDto;

import java.time.OffsetDateTime;

/** Operator dashboard use cases (HU-16): aggregated KPIs and single-round detail, over {@link OperatorDashboardPort}. */
public class OperatorDashboardUseCase {

    private final OperatorDashboardPort port;

    public OperatorDashboardUseCase(final OperatorDashboardPort port) {
        this.port = port;
    }

    public DashboardDto dashboard(final Long operatorId, final OffsetDateTime from, final OffsetDateTime to) {
        return port.dashboard(operatorId, from, to);
    }

    public RoundDetailDto roundDetail(final Long roundId, final Long operatorId) {
        return port.roundDetail(roundId, operatorId)
                .orElseThrow(() -> new RoundNotFoundException(roundId));
    }
}
