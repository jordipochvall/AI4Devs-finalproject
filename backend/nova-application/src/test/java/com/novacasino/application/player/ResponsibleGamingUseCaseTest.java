package com.novacasino.application.player;

import com.novacasino.application.player.exception.LimitReachedException;
import com.novacasino.application.player.exception.SelfExcludedException;
import com.novacasino.common.dto.LimitDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Unit tests for HU-19 responsible gaming in {@link ResponsibleGamingUseCase} (port mocked). */
class ResponsibleGamingUseCaseTest {

    private static final long USER = 42L;
    private static final long COOLDOWN = 86400L;

    private ResponsibleGamingPort store;
    private ResponsibleGamingUseCase useCase;

    @BeforeEach
    void setUp() {
        store = mock(ResponsibleGamingPort.class);
        useCase = new ResponsibleGamingUseCase(store, COOLDOWN);
    }

    private static PlayerLimit limit(final long amount, final Long pendingAmount) {
        return new PlayerLimit(1L, USER, "LOSS", "DAILY", amount, OffsetDateTime.now(),
                pendingAmount, pendingAmount == null ? null : OffsetDateTime.now().plusSeconds(COOLDOWN));
    }

    @Test
    void setLimit_firstTime_appliesImmediately() {
        when(store.findLimit(USER, "LOSS", "DAILY")).thenReturn(Optional.empty());
        when(store.createLimit(eq(USER), eq("LOSS"), eq("DAILY"), eq(5000L), any()))
                .thenReturn(limit(5000L, null));

        final LimitDto dto = useCase.setLimit(USER, "LOSS", "DAILY", 5000L);

        assertThat(dto.amountCents()).isEqualTo(5000L);
        assertThat(dto.pendingAmountCents()).isNull();
    }

    @Test
    void setLimit_lowering_appliesImmediately() {
        when(store.findLimit(USER, "LOSS", "DAILY")).thenReturn(Optional.of(limit(5000L, null)));
        when(store.updateLimit(eq(1L), eq(2000L), any(), isNull(), isNull()))
                .thenReturn(limit(2000L, null));

        final LimitDto dto = useCase.setLimit(USER, "LOSS", "DAILY", 2000L);

        assertThat(dto.amountCents()).isEqualTo(2000L);
        assertThat(dto.pendingAmountCents()).isNull();
    }

    @Test
    void setLimit_raising_isDeferred() {
        when(store.findLimit(USER, "LOSS", "DAILY")).thenReturn(Optional.of(limit(2000L, null)));
        when(store.updateLimit(eq(1L), eq(2000L), any(), eq(9000L), any()))
                .thenReturn(limit(2000L, 9000L));

        final LimitDto dto = useCase.setLimit(USER, "LOSS", "DAILY", 9000L);

        assertThat(dto.amountCents()).isEqualTo(2000L);
        assertThat(dto.pendingAmountCents()).isEqualTo(9000L);
    }

    @Test
    void assertCanSpin_lossLimitReached_throws() {
        when(store.isSelfExcluded(eq(USER), any())).thenReturn(false);
        when(store.findLimitsByUser(USER)).thenReturn(List.of(limit(1000L, null)));
        when(store.netLossSince(eq(USER), any())).thenReturn(1500L);

        assertThatThrownBy(() -> useCase.assertCanSpin(USER)).isInstanceOf(LimitReachedException.class);
    }

    @Test
    void assertCanSpin_underLimit_passes() {
        when(store.isSelfExcluded(eq(USER), any())).thenReturn(false);
        when(store.findLimitsByUser(USER)).thenReturn(List.of(limit(1000L, null)));
        when(store.netLossSince(eq(USER), any())).thenReturn(200L);

        assertThatCode(() -> useCase.assertCanSpin(USER)).doesNotThrowAnyException();
    }

    @Test
    void assertCanSpin_selfExcluded_throws() {
        when(store.isSelfExcluded(eq(USER), any())).thenReturn(true);

        assertThatThrownBy(() -> useCase.assertCanSpin(USER)).isInstanceOf(SelfExcludedException.class);
        verify(store, never()).findLimitsByUser(any());
    }
}
