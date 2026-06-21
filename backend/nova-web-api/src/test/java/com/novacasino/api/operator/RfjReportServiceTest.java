package com.novacasino.api.operator;

import com.novacasino.api.operator.dto.IntegrityReportDto;
import com.novacasino.api.operator.dto.RfjReportDto;
import com.novacasino.api.operator.exception.ReportIntegrityException;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-21 RFJ report generation + integrity gate in {@link RfjReportService}. */
class RfjReportServiceTest {

    private static final long OPERATOR_ID = 1L;

    private GameRoundJpaRepository roundRepo;
    private IntegrityService integrityService;
    private RfjReportService service;

    @BeforeEach
    void setUp() {
        roundRepo = mock(GameRoundJpaRepository.class);
        integrityService = mock(IntegrityService.class);
        service = new RfjReportService(roundRepo, integrityService);
    }

    @Test
    void generate_intactPeriod_aggregatesAndStampsIntegrity() {
        when(integrityService.verify(eq(OPERATOR_ID), any(), any()))
                .thenReturn(new IntegrityReportDto(OffsetDateTime.now(), OffsetDateTime.now(), 40, true, null));
        when(roundRepo.sumBetCents(eq(OPERATOR_ID), any(), any())).thenReturn(100_000L);
        when(roundRepo.sumWinCents(eq(OPERATOR_ID), any(), any())).thenReturn(94_000L);
        when(roundRepo.countRounds(eq(OPERATOR_ID), any(), any())).thenReturn(40L);
        when(roundRepo.countActivePlayers(eq(OPERATOR_ID), any(), any())).thenReturn(7L);

        final RfjReportDto report = service.generate(OPERATOR_ID, 2026, 5);

        assertThat(report.totalWageredCents()).isEqualTo(100_000L);
        assertThat(report.totalWonCents()).isEqualTo(94_000L);
        assertThat(report.ggrCents()).isEqualTo(6_000L);
        assertThat(report.totalRounds()).isEqualTo(40L);
        assertThat(report.activePlayers()).isEqualTo(7L);
        assertThat(report.integrityConsistent()).isTrue();
        // The period spans the calendar month.
        assertThat(report.periodFrom().getMonthValue()).isEqualTo(5);
        assertThat(report.periodTo().getMonthValue()).isEqualTo(6);
    }

    @Test
    void generate_brokenIntegrity_isBlocked() {
        when(integrityService.verify(eq(OPERATOR_ID), any(), any()))
                .thenReturn(new IntegrityReportDto(OffsetDateTime.now(), OffsetDateTime.now(), 10, false, 33L));

        assertThatThrownBy(() -> service.generate(OPERATOR_ID, 2026, 5))
                .isInstanceOf(ReportIntegrityException.class);
        // No aggregation is attempted when integrity fails.
        verify(roundRepo, never()).sumBetCents(any(), any(), any());
    }
}
