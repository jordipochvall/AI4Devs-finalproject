package com.novacasino.application.operator;

import com.novacasino.application.audit.VerifyIntegrityUseCase;
import com.novacasino.application.operator.RfjAggregatesPort.RfjAggregates;
import com.novacasino.application.operator.exception.ReportIntegrityException;
import com.novacasino.common.dto.IntegrityReportDto;
import com.novacasino.common.dto.RfjReportDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Generates the RFJ regulatory report for a calendar month (HU-21). It first verifies the period's
 * integrity chain (HU-20) and refuses to report over tampered data (AC3); otherwise it aggregates the
 * auditable {@code game_rounds} and stamps the integrity status into the document (AC2).
 */
public class RfjReportUseCase {

    private static final Logger log = LoggerFactory.getLogger(RfjReportUseCase.class);

    private final VerifyIntegrityUseCase integrity;
    private final RfjAggregatesPort port;

    public RfjReportUseCase(final VerifyIntegrityUseCase integrity, final RfjAggregatesPort port) {
        this.integrity = integrity;
        this.port = port;
    }

    /** Builds the RFJ report for {@code year}/{@code month}, blocking on a broken integrity chain. */
    public RfjReportDto generate(final Long operatorId, final int year, final int month) {
        final OffsetDateTime from = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime to = from.plusMonths(1);

        // AC3: do not report over manipulated data.
        final IntegrityReportDto report = integrity.verify(operatorId, from, to);
        if (!report.consistent()) {
            throw new ReportIntegrityException(report.firstBrokenRoundId());
        }

        final RfjAggregates agg = port.aggregate(operatorId, from, to);
        log.info("RFJ report generated: operatorId={}, period={}-{}, rounds={}, ggr={} cents",
                operatorId, year, month, agg.totalRounds(),
                agg.totalWageredCents() - agg.totalWonCents());
        return new RfjReportDto(
                operatorId, from, to,
                agg.totalRounds(), agg.activePlayers(),
                agg.totalWageredCents(), agg.totalWonCents(),
                agg.totalWageredCents() - agg.totalWonCents(),
                report.consistent(), report.checked(),
                OffsetDateTime.now());
    }
}
