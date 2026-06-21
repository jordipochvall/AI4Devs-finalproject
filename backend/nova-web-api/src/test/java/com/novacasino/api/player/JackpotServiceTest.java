package com.novacasino.api.player;

import com.novacasino.infrastructure.persistence.entity.JackpotPoolEntity;
import com.novacasino.infrastructure.persistence.repository.JackpotGrantJpaRepository;
import com.novacasino.infrastructure.persistence.repository.JackpotPoolJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Unit tests for HU-26 jackpot determinism and pool arithmetic in {@link JackpotService}. */
class JackpotServiceTest {

    private JackpotPoolJpaRepository poolRepo;
    private JackpotGrantJpaRepository grantRepo;
    private JackpotService service;

    @BeforeEach
    void setUp() {
        poolRepo = mock(JackpotPoolJpaRepository.class);
        grantRepo = mock(JackpotGrantJpaRepository.class);
        service = new JackpotService(poolRepo, grantRepo);
    }

    // --- AC2: the grant decision is deterministic for a given seed + odds ---

    @Test
    void isAwarded_isDeterministicForSameSeed() {
        final long seed = 123456789L;
        final boolean first = JackpotService.isAwarded(seed, 1000L);
        for (int i = 0; i < 5; i++) {
            assertThat(JackpotService.isAwarded(seed, 1000L)).isEqualTo(first); // reproducible
        }
        // Odds of 1 always award; an "impossible" odds set never does for this seed sample.
        assertThat(JackpotService.isAwarded(seed, 1L)).isTrue();
    }

    // --- AC1: integer contribution = bet × bps / 10000 ---

    @Test
    void contribution_isIntegerFraction() {
        final JackpotPoolEntity pool = new JackpotPoolEntity(3L, 100_000L, 100_000L, 100, 1L); // 1%
        assertThat(service.contribution(pool, 10_000L)).isEqualTo(100L);
        assertThat(service.contribution(pool, 250L)).isEqualTo(2L); // 250*100/10000 = 2 (floor)
    }

    // --- AC3: awarding resets the pool to its seed; otherwise it grows by the contribution ---

    @Test
    void applyOutcome_resetsOnAwardAndGrowsOtherwise() {
        when(poolRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final JackpotPoolEntity awarded = new JackpotPoolEntity(3L, 500_000L, 100_000L, 100, 1L);
        service.applyOutcome(awarded, true, 50L);
        assertThat(awarded.getCurrentCents()).isEqualTo(100_000L); // reset to seed

        final JackpotPoolEntity grown = new JackpotPoolEntity(3L, 500_000L, 100_000L, 100, 1L);
        service.applyOutcome(grown, false, 50L);
        assertThat(grown.getCurrentCents()).isEqualTo(500_050L); // grew by contribution
        verify(poolRepo, times(2)).save(any());
    }
}
