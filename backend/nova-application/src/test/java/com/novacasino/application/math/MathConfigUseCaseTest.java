package com.novacasino.application.math;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.application.math.MathConfigPort.ConfigRef;
import com.novacasino.application.math.MathConfigPort.OwnedGame;
import com.novacasino.application.math.exception.ConfigAlreadyActiveException;
import com.novacasino.application.math.exception.ConfigNotFoundException;
import com.novacasino.application.math.validation.ConfigValidationException;
import com.novacasino.application.math.validation.ConfigValidator;
import com.novacasino.common.dto.ConfigCreatedDto;
import com.novacasino.common.dto.PublishResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-17 ownership/publish/create rules in {@link MathConfigUseCase} (port + validator mocked). */
class MathConfigUseCaseTest {

    private static final long OPERATOR_ID = 1L;
    private static final long GAME_ID = 10L;
    private static final long MATH_USER = 99L;

    private MathConfigPort port;
    private ConfigValidator validator;
    private MathConfigUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(MathConfigPort.class);
        validator = mock(ConfigValidator.class);
        useCase = new MathConfigUseCase(port, validator);
    }

    @Test
    void listConfigs_gameOfAnotherOperator_throwsNotFound() {
        when(port.findOwnedGame(GAME_ID, OPERATOR_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.listConfigs(GAME_ID, OPERATOR_ID))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void publish_movesActivePointer() {
        when(port.findOwnedGame(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(new OwnedGame(1L)));
        when(port.findConfig(2L)).thenReturn(Optional.of(new ConfigRef(2L, GAME_ID, 2)));
        final PublishResultDto result = new PublishResultDto(GAME_ID, 2L, 2, MATH_USER, OffsetDateTime.now());
        when(port.applyPublish(GAME_ID, 2L, OPERATOR_ID, MATH_USER)).thenReturn(result);

        assertThat(useCase.publish(GAME_ID, 2L, OPERATOR_ID, MATH_USER).activeConfigId()).isEqualTo(2L);
    }

    @Test
    void publish_alreadyActive_throwsConflict() {
        when(port.findOwnedGame(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(new OwnedGame(2L)));
        when(port.findConfig(2L)).thenReturn(Optional.of(new ConfigRef(2L, GAME_ID, 2)));

        assertThatThrownBy(() -> useCase.publish(GAME_ID, 2L, OPERATOR_ID, MATH_USER))
                .isInstanceOf(ConfigAlreadyActiveException.class);
        verify(port, never()).applyPublish(any(), any(), any(), any());
    }

    @Test
    void publish_configOfAnotherGame_throwsNotFound() {
        when(port.findOwnedGame(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(new OwnedGame(1L)));
        when(port.findConfig(5L)).thenReturn(Optional.of(new ConfigRef(5L, 999L, 1))); // different game

        assertThatThrownBy(() -> useCase.publish(GAME_ID, 5L, OPERATOR_ID, MATH_USER))
                .isInstanceOf(ConfigNotFoundException.class);
    }

    @Test
    void getConfig_missing_throwsNotFound() {
        when(port.getConfig(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.getConfig(7L)).isInstanceOf(ConfigNotFoundException.class);
    }

    @Test
    void createConfig_rtpOutOfRange_throws() {
        when(port.findOwnedGame(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(new OwnedGame(1L)));
        final CreateConfigCommand cmd = new CreateConfigCommand(null, new BigDecimal("2"), null, null);

        assertThatThrownBy(() -> useCase.createConfig(GAME_ID, OPERATOR_ID, MATH_USER, cmd))
                .isInstanceOf(ConfigValidationException.class);
        verify(port, never()).createConfig(any(), any(), any());
    }

    @Test
    void createConfig_valid_persists() {
        when(port.findOwnedGame(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(new OwnedGame(1L)));
        final CreateConfigCommand cmd = new CreateConfigCommand(null, new BigDecimal("0.95"), null, null);
        final ConfigCreatedDto dto = new ConfigCreatedDto(3L, GAME_ID, 2, new BigDecimal("0.95"), null);
        when(port.createConfig(eq(GAME_ID), eq(MATH_USER), any())).thenReturn(dto);

        assertThat(useCase.createConfig(GAME_ID, OPERATOR_ID, MATH_USER, cmd).id()).isEqualTo(3L);
        verify(validator).validate(any());
    }
}
