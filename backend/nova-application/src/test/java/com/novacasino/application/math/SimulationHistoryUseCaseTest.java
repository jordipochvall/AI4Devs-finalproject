package com.novacasino.application.math;

import com.novacasino.application.math.exception.SimulationNotFoundException;
import com.novacasino.common.dto.ExplanationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** Unit tests for HU-18 history reads in {@link SimulationHistoryUseCase} (port mocked). */
class SimulationHistoryUseCaseTest {

    private SimulationQueryPort port;
    private SimulationHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(SimulationQueryPort.class);
        useCase = new SimulationHistoryUseCase(port);
    }

    @Test
    void listExplanations_present_returnsThread() {
        final ExplanationDto e = new ExplanationDto("q", "a", "m", OffsetDateTime.now());
        when(port.listExplanations(5L, 1L)).thenReturn(Optional.of(List.of(e)));
        assertThat(useCase.listExplanations(5L, 1L)).containsExactly(e);
    }

    @Test
    void listExplanations_missing_throwsNotFound() {
        when(port.listExplanations(5L, 1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.listExplanations(5L, 1L))
                .isInstanceOf(SimulationNotFoundException.class);
    }
}
