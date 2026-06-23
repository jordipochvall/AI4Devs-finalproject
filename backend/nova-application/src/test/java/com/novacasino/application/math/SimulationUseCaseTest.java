package com.novacasino.application.math;

import com.novacasino.application.math.exception.ConfigNotFoundException;
import com.novacasino.application.math.exception.InvalidSimulationParamsException;
import com.novacasino.application.math.exception.SimulationNotFoundException;
import com.novacasino.common.dto.SimulationAcceptedDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Unit tests for HU-2 launch validation in {@link SimulationUseCase} (port mocked). */
class SimulationUseCaseTest {

    private static final long OPERATOR = 1L;
    private static final long USER = 9L;
    private static final long CONFIG = 100L;

    private SimulationLaunchPort port;
    private SimulationUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(SimulationLaunchPort.class);
        useCase = new SimulationUseCase(port);
    }

    @Test
    void launch_numSpinsOutOfRange_throws() {
        assertThatThrownBy(() -> useCase.launch(OPERATOR, USER, CONFIG, 0L, 100L))
                .isInstanceOf(InvalidSimulationParamsException.class);
        assertThatThrownBy(() -> useCase.launch(OPERATOR, USER, CONFIG, 10_000_001L, 100L))
                .isInstanceOf(InvalidSimulationParamsException.class);
        verify(port, never()).ownedConfigPaylineCount(any(), any());
    }

    @Test
    void launch_configNotFound_throws() {
        when(port.ownedConfigPaylineCount(CONFIG, OPERATOR)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.launch(OPERATOR, USER, CONFIG, 1000L, 100L))
                .isInstanceOf(ConfigNotFoundException.class);
    }

    @Test
    void launch_betNotMultipleOfPaylines_throws() {
        when(port.ownedConfigPaylineCount(CONFIG, OPERATOR)).thenReturn(Optional.of(5));
        assertThatThrownBy(() -> useCase.launch(OPERATOR, USER, CONFIG, 1000L, 501L))
                .isInstanceOf(InvalidSimulationParamsException.class);
        verify(port, never()).createAndLaunch(any(), any(), any(), anyLong(), anyLong());
    }

    @Test
    void launch_valid_createsAndLaunches() {
        when(port.ownedConfigPaylineCount(CONFIG, OPERATOR)).thenReturn(Optional.of(5));
        final SimulationAcceptedDto accepted =
                new SimulationAcceptedDto(7L, "RUNNING", OffsetDateTime.now(), "/api/v1/math/simulations/7");
        when(port.createAndLaunch(OPERATOR, USER, CONFIG, 1000L, 500L)).thenReturn(accepted);

        assertThat(useCase.launch(OPERATOR, USER, CONFIG, 1000L, 500L).simulationId()).isEqualTo(7L);
    }

    @Test
    void getSimulation_missing_throwsNotFound() {
        when(port.getSimulation(7L, OPERATOR)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.getSimulation(7L, OPERATOR))
                .isInstanceOf(SimulationNotFoundException.class);
    }
}
