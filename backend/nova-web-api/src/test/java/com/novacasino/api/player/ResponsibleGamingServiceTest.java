package com.novacasino.api.player;

import com.novacasino.api.player.dto.LimitDto;
import com.novacasino.api.player.exception.LimitReachedException;
import com.novacasino.api.player.exception.SelfExcludedException;
import com.novacasino.infrastructure.persistence.entity.PlayerLimitEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.PlayerLimitJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SelfExclusionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-19 responsible-gaming enforcement in {@link ResponsibleGamingService}. */
class ResponsibleGamingServiceTest {

    private static final long USER_ID = 42L;
    private static final long COOLDOWN = 86400L;

    private PlayerLimitJpaRepository limitRepo;
    private SelfExclusionJpaRepository exclusionRepo;
    private GameRoundJpaRepository roundRepo;
    private ResponsibleGamingService service;

    @BeforeEach
    void setUp() {
        limitRepo = mock(PlayerLimitJpaRepository.class);
        exclusionRepo = mock(SelfExclusionJpaRepository.class);
        roundRepo = mock(GameRoundJpaRepository.class);
        when(limitRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new ResponsibleGamingService(limitRepo, exclusionRepo, roundRepo, COOLDOWN);
    }

    // --- AC3: hardening applies immediately; relaxing is deferred by the cooldown ---

    @Test
    void setLimit_firstTime_appliesImmediately() {
        when(limitRepo.findByUserIdAndLimitTypeAndPeriod(USER_ID, "LOSS", "DAILY")).thenReturn(Optional.empty());

        final LimitDto dto = service.setLimit(USER_ID, "LOSS", "DAILY", 5000L);

        assertThat(dto.amountCents()).isEqualTo(5000L);
        assertThat(dto.pendingAmountCents()).isNull();
    }

    @Test
    void setLimit_lowering_appliesImmediately() {
        final PlayerLimitEntity existing = new PlayerLimitEntity(USER_ID, "LOSS", "DAILY", 5000L, OffsetDateTime.now());
        when(limitRepo.findByUserIdAndLimitTypeAndPeriod(USER_ID, "LOSS", "DAILY")).thenReturn(Optional.of(existing));

        final LimitDto dto = service.setLimit(USER_ID, "LOSS", "DAILY", 2000L);

        assertThat(dto.amountCents()).isEqualTo(2000L);   // tightened now
        assertThat(dto.pendingAmountCents()).isNull();
    }

    @Test
    void setLimit_raising_isDeferred() {
        final PlayerLimitEntity existing = new PlayerLimitEntity(USER_ID, "LOSS", "DAILY", 2000L, OffsetDateTime.now());
        when(limitRepo.findByUserIdAndLimitTypeAndPeriod(USER_ID, "LOSS", "DAILY")).thenReturn(Optional.of(existing));

        final LimitDto dto = service.setLimit(USER_ID, "LOSS", "DAILY", 9000L);

        assertThat(dto.amountCents()).isEqualTo(2000L);          // still the old, lower limit
        assertThat(dto.pendingAmountCents()).isEqualTo(9000L);   // raise staged
        assertThat(dto.pendingEffectiveAt()).isNotNull();
    }

    // --- AC2: a reached loss limit blocks the spin ---

    @Test
    void assertCanSpin_lossLimitReached_throws() {
        final PlayerLimitEntity limit = new PlayerLimitEntity(USER_ID, "LOSS", "DAILY", 1000L, OffsetDateTime.now());
        when(exclusionRepo.existsByUserIdAndEndAtAfter(eq(USER_ID), any())).thenReturn(false);
        when(limitRepo.findByUserId(USER_ID)).thenReturn(List.of(limit));
        when(roundRepo.netLossSince(eq(USER_ID), any())).thenReturn(1500L); // loss exceeds the 1000 limit

        assertThatThrownBy(() -> service.assertCanSpin(USER_ID)).isInstanceOf(LimitReachedException.class);
    }

    @Test
    void assertCanSpin_underLimit_passes() {
        final PlayerLimitEntity limit = new PlayerLimitEntity(USER_ID, "LOSS", "DAILY", 1000L, OffsetDateTime.now());
        when(exclusionRepo.existsByUserIdAndEndAtAfter(eq(USER_ID), any())).thenReturn(false);
        when(limitRepo.findByUserId(USER_ID)).thenReturn(List.of(limit));
        when(roundRepo.netLossSince(eq(USER_ID), any())).thenReturn(200L);

        assertThatCode(() -> service.assertCanSpin(USER_ID)).doesNotThrowAnyException();
    }

    // --- AC2: an active self-exclusion blocks the spin (before any limit check) ---

    @Test
    void assertCanSpin_selfExcluded_throws() {
        when(exclusionRepo.existsByUserIdAndEndAtAfter(eq(USER_ID), any())).thenReturn(true);

        assertThatThrownBy(() -> service.assertCanSpin(USER_ID)).isInstanceOf(SelfExcludedException.class);
        verify(limitRepo, never()).findByUserId(any());
    }
}
