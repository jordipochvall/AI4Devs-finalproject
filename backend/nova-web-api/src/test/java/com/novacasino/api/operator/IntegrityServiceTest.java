package com.novacasino.api.operator;

import com.novacasino.api.operator.dto.IntegrityReportDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-20 chain verification logic in {@link IntegrityService}. */
class IntegrityServiceTest {

    private static final long OPERATOR_ID = 1L;

    private GameRoundJpaRepository roundRepo;
    private IntegrityService service;

    @BeforeEach
    void setUp() {
        roundRepo = mock(GameRoundJpaRepository.class);
        service = new IntegrityService(roundRepo);
    }

    @Test
    void verify_intactChain_isConsistent() {
        final GameRoundEntity r1 = chained(10L, "");
        final GameRoundEntity r2 = chained(11L, hashOf(10L, ""));
        when(roundRepo.findByOperatorIdAndCreatedAtBetweenOrderByIdAsc(eq(OPERATOR_ID), any(), any()))
                .thenReturn(List.of(r1, r2));

        final IntegrityReportDto report = service.verify(OPERATOR_ID, null, null);

        assertThat(report.consistent()).isTrue();
        assertThat(report.checked()).isEqualTo(2);
        assertThat(report.firstBrokenRoundId()).isNull();
    }

    @Test
    void verify_tamperedRow_reportsFirstBreak() {
        final GameRoundEntity r1 = chained(10L, "");
        final GameRoundEntity r2 = chained(11L, hashOf(10L, ""));
        setField(r2, "rowHash", "0".repeat(64)); // someone overwrote the stored hash
        when(roundRepo.findByOperatorIdAndCreatedAtBetweenOrderByIdAsc(eq(OPERATOR_ID), any(), any()))
                .thenReturn(List.of(r1, r2));

        final IntegrityReportDto report = service.verify(OPERATOR_ID, null, null);

        assertThat(report.consistent()).isFalse();
        assertThat(report.firstBrokenRoundId()).isEqualTo(11L);
    }

    @Test
    void verify_emptyChain_isConsistent() {
        when(roundRepo.findByOperatorIdAndCreatedAtBetweenOrderByIdAsc(eq(OPERATOR_ID), any(), any()))
                .thenReturn(List.of());
        final IntegrityReportDto report = service.verify(OPERATOR_ID, null, null);
        assertThat(report.consistent()).isTrue();
        assertThat(report.checked()).isZero();
    }

    // -------------------------------------------------------------------------
    // Helpers — build a round whose stored hashes match the service's algorithm.
    // Fixed fields: playerId=2, gameId=3, bet=100, win=0, balancePost=900, rngSeed=42.
    // -------------------------------------------------------------------------

    private static GameRoundEntity chained(final long id, final String prev) {
        final GameRoundEntity r = GameRoundEntity.baseRound(
                OPERATOR_ID, 2L, 3L, 100L, 42L, 100L, 0L, 1000L, 900L, "{}");
        setField(r, "id", id);
        setField(r, "prevHash", prev);
        setField(r, "rowHash", hashOf(id, prev));
        return r;
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

    private static void setField(final Object entity, final String field, final Object value) {
        try {
            final var f = entity.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(entity, value);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
