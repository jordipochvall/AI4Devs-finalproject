package com.novacasino.application.audit;

import com.novacasino.common.dto.IntegrityReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-20 chain verification in {@link VerifyIntegrityUseCase} (port mocked). */
class VerifyIntegrityUseCaseTest {

    private static final long OPERATOR_ID = 1L;

    private AuditChainPort auditChain;
    private VerifyIntegrityUseCase useCase;

    @BeforeEach
    void setUp() {
        auditChain = mock(AuditChainPort.class);
        useCase = new VerifyIntegrityUseCase(auditChain);
    }

    @Test
    void verify_intactChain_isConsistent() {
        when(auditChain.roundsInWindow(eq(OPERATOR_ID), any(), any()))
                .thenReturn(List.of(chained(10L, ""), chained(11L, hashOf(10L, ""))));

        final IntegrityReportDto report = useCase.verify(OPERATOR_ID, null, null);

        assertThat(report.consistent()).isTrue();
        assertThat(report.checked()).isEqualTo(2);
        assertThat(report.firstBrokenRoundId()).isNull();
    }

    @Test
    void verify_tamperedRow_reportsFirstBreak() {
        final AuditRound r1 = chained(10L, "");
        final AuditRound r2tampered = new AuditRound(11L, 2L, 3L, 100L, 0L, 900L, 42L, hashOf(10L, ""), "0".repeat(64));
        when(auditChain.roundsInWindow(eq(OPERATOR_ID), any(), any())).thenReturn(List.of(r1, r2tampered));

        final IntegrityReportDto report = useCase.verify(OPERATOR_ID, null, null);

        assertThat(report.consistent()).isFalse();
        assertThat(report.firstBrokenRoundId()).isEqualTo(11L);
    }

    @Test
    void verify_emptyChain_isConsistent() {
        when(auditChain.roundsInWindow(eq(OPERATOR_ID), any(), any())).thenReturn(List.of());
        final IntegrityReportDto report = useCase.verify(OPERATOR_ID, null, null);
        assertThat(report.consistent()).isTrue();
        assertThat(report.checked()).isZero();
    }

    // Fixed fields: player=2, game=3, bet=100, win=0, balancePost=900, rngSeed=42.
    private static AuditRound chained(final long id, final String prev) {
        return new AuditRound(id, 2L, 3L, 100L, 0L, 900L, 42L, prev, hashOf(id, prev));
    }

    private static String hashOf(final long id, final String prev) {
        final String canonical = prev + "|" + id + "|2|3|100|0|900|42";
        try {
            final byte[] d = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(64);
            for (final byte b : d) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
