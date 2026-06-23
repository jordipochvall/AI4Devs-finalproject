package com.novacasino.application.operator;

import com.novacasino.application.exception.RoundNotFoundException;
import com.novacasino.common.dto.DashboardDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for HU-16 dashboard delegation in {@link OperatorDashboardUseCase} (port mocked). */
class OperatorDashboardUseCaseTest {

    private OperatorDashboardPort port;
    private OperatorDashboardUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(OperatorDashboardPort.class);
        useCase = new OperatorDashboardUseCase(port);
    }

    @Test
    void dashboard_delegatesToPort() {
        final DashboardDto dto = new DashboardDto(null, null, 3, 1000, 50, List.of());
        when(port.dashboard(eq(1L), any(), any())).thenReturn(dto);
        assertThat(useCase.dashboard(1L, null, null)).isSameAs(dto);
    }

    @Test
    void roundDetail_missing_throwsNotFound() {
        when(port.roundDetail(99L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.roundDetail(99L, 1L)).isInstanceOf(RoundNotFoundException.class);
    }
}
