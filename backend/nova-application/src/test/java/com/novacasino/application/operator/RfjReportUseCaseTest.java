package com.novacasino.application.operator;

import com.novacasino.application.audit.VerifyIntegrityUseCase;
import com.novacasino.application.operator.RfjAggregatesPort.RfjAggregates;
import com.novacasino.application.operator.exception.ReportIntegrityException;
import com.novacasino.common.dto.IntegrityReportDto;
import com.novacasino.common.dto.RfjReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-21 RFJ report generation + integrity gate in {@link RfjReportUseCase}. */
class RfjReportUseCaseTest {

    private static final long OPERATOR_ID = 1L;

    private VerifyIntegrityUseCase integrity;
    private RfjAggregatesPort port;
    private RfjReportUseCase useCase;

    @BeforeEach
    void setUp() {
        integrity = mock(VerifyIntegrityUseCase.class);
        port = mock(RfjAggregatesPort.class);
        useCase = new RfjReportUseCase(integrity, port);
    }

    @Test
    void generate_intactPeriod_aggregatesAndStampsIntegrity() {
        when(integrity.verify(eq(OPERATOR_ID), any(), any()))
                .thenReturn(new IntegrityReportDto(OffsetDateTime.now(), OffsetDateTime.now(), 40, true, null));
        when(port.aggregate(eq(OPERATOR_ID), any(), any()))
                .thenReturn(new RfjAggregates(100_000L, 94_000L, 40L, 7L));

        final RfjReportDto report = useCase.generate(OPERATOR_ID, 2026, 5);

        assertThat(report.totalWageredCents()).isEqualTo(100_000L);
        assertThat(report.totalWonCents()).isEqualTo(94_000L);
        assertThat(report.ggrCents()).isEqualTo(6_000L);
        assertThat(report.totalRounds()).isEqualTo(40L);
        assertThat(report.activePlayers()).isEqualTo(7L);
        assertThat(report.integrityConsistent()).isTrue();
        assertThat(report.periodFrom().getMonthValue()).isEqualTo(5);
        assertThat(report.periodTo().getMonthValue()).isEqualTo(6);
    }

    @Test
    void generate_brokenIntegrity_isBlocked() {
        when(integrity.verify(eq(OPERATOR_ID), any(), any()))
                .thenReturn(new IntegrityReportDto(OffsetDateTime.now(), OffsetDateTime.now(), 10, false, 33L));

        assertThatThrownBy(() -> useCase.generate(OPERATOR_ID, 2026, 5))
                .isInstanceOf(ReportIntegrityException.class);
        verify(port, never()).aggregate(any(), any(), any());
    }
}
