package com.novacasino.api.operator;

import com.novacasino.api.operator.dto.IntegrityReportDto;
import com.novacasino.api.operator.dto.RfjReportDto;
import com.novacasino.api.operator.exception.ReportIntegrityException;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Generates the RFJ regulatory report for a calendar month (HU-21). It first verifies the period's
 * integrity chain (HU-20) and refuses to report over tampered data (AC3); otherwise it aggregates the
 * auditable {@code game_rounds} and stamps the integrity status into the document (AC2).
 */
@Service
public class RfjReportService {

    private final GameRoundJpaRepository roundRepo;
    private final IntegrityService integrityService;

    public RfjReportService(final GameRoundJpaRepository roundRepo, final IntegrityService integrityService) {
        this.roundRepo = roundRepo;
        this.integrityService = integrityService;
    }

    /** Builds the RFJ report for {@code year}/{@code month}, blocking on a broken integrity chain. */
    @Transactional(readOnly = true)
    public RfjReportDto generate(final Long operatorId, final int year, final int month) {
        final OffsetDateTime from = OffsetDateTime.of(year, month, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        final OffsetDateTime to = from.plusMonths(1);

        // AC3: do not report over manipulated data.
        final IntegrityReportDto integrity = integrityService.verify(operatorId, from, to);
        if (!integrity.consistent()) {
            throw new ReportIntegrityException(integrity.firstBrokenRoundId());
        }

        final long wagered = roundRepo.sumBetCents(operatorId, from, to);
        final long won = roundRepo.sumWinCents(operatorId, from, to);
        return new RfjReportDto(
                operatorId, from, to,
                roundRepo.countRounds(operatorId, from, to),
                roundRepo.countActivePlayers(operatorId, from, to),
                wagered, won, wagered - won,
                integrity.consistent(), integrity.checked(),
                OffsetDateTime.now());
    }
}
