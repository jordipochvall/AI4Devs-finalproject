package com.novacasino.application.operator;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.application.operator.exception.InvalidCommercialConfigException;
import com.novacasino.common.dto.OperatorGameDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-15 commercial-config validation in {@link OperatorGameUseCase} (port mocked). */
class OperatorGameUseCaseTest {

    private static final long OPERATOR_ID = 1L;
    private static final long GAME_ID = 10L;
    private static final long USER_ID = 7L;

    private OperatorGamePort port;
    private OperatorGameUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(OperatorGamePort.class);
        useCase = new OperatorGameUseCase(port);
    }

    private static GameCommercialUpdate update(final long min, final long max, final long step) {
        return new GameCommercialUpdate(min, max, step, true, List.of("EUR"));
    }

    @Test
    void updateGame_valid_appliesUpdate() {
        when(port.findGamePaylineCount(OPERATOR_ID, GAME_ID)).thenReturn(Optional.of(5));
        final OperatorGameDto dto = new OperatorGameDto(GAME_ID, "c", "n", "t",
                500, 20000, 500, List.of("EUR"), true, 100L, 5);
        when(port.applyUpdate(eq(OPERATOR_ID), eq(GAME_ID), eq(USER_ID), any())).thenReturn(dto);

        useCase.updateGame(OPERATOR_ID, GAME_ID, USER_ID, update(500, 20000, 500));

        verify(port).applyUpdate(eq(OPERATOR_ID), eq(GAME_ID), eq(USER_ID), any());
    }

    @Test
    void updateGame_betNotMultipleOfPaylines_throwsAndDoesNotPersist() {
        when(port.findGamePaylineCount(OPERATOR_ID, GAME_ID)).thenReturn(Optional.of(5));

        // 501 is not a multiple of 5 paylines.
        assertThatThrownBy(() -> useCase.updateGame(OPERATOR_ID, GAME_ID, USER_ID, update(501, 20000, 500)))
                .isInstanceOf(InvalidCommercialConfigException.class);
        verify(port, never()).applyUpdate(any(), any(), any(), any());
    }

    @Test
    void updateGame_maxLessThanMin_throws() {
        when(port.findGamePaylineCount(OPERATOR_ID, GAME_ID)).thenReturn(Optional.of(5));

        assertThatThrownBy(() -> useCase.updateGame(OPERATOR_ID, GAME_ID, USER_ID, update(1000, 500, 100)))
                .isInstanceOf(InvalidCommercialConfigException.class);
        verify(port, never()).applyUpdate(any(), any(), any(), any());
    }

    @Test
    void updateGame_gameOfAnotherOperator_throwsNotFound() {
        when(port.findGamePaylineCount(OPERATOR_ID, GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.updateGame(OPERATOR_ID, GAME_ID, USER_ID, update(500, 20000, 500)))
                .isInstanceOf(GameNotFoundException.class);
        verify(port, never()).applyUpdate(any(), any(), any(), any());
    }
}
