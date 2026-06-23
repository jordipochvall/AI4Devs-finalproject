package com.novacasino.application.operator;

import com.novacasino.application.operator.exception.InvalidAmountException;
import com.novacasino.application.operator.exception.PlayerNotFoundException;
import com.novacasino.common.dto.WalletDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/** Unit tests for HU-6 recharge rules in {@link OperatorPlayerUseCase} (port mocked). */
class OperatorPlayerUseCaseTest {

    private static final long OP_USER = 7L;
    private static final long OPERATOR = 1L;
    private static final long PLAYER = 42L;

    private OperatorPlayerPort port;
    private OperatorPlayerUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(OperatorPlayerPort.class);
        useCase = new OperatorPlayerUseCase(port);
    }

    @Test
    void recharge_valid_returnsNewBalance() {
        when(port.recharge(OP_USER, OPERATOR, PLAYER, 5000L))
                .thenReturn(Optional.of(new WalletDto(15000L, "EUR")));

        final WalletDto dto = useCase.recharge(OP_USER, OPERATOR, PLAYER, 5000L);

        assertThat(dto.balanceCents()).isEqualTo(15000L);
    }

    @Test
    void recharge_nonPositiveAmount_throwsAndDoesNotPersist() {
        assertThatThrownBy(() -> useCase.recharge(OP_USER, OPERATOR, PLAYER, 0L))
                .isInstanceOf(InvalidAmountException.class);
        verify(port, never()).recharge(anyLong(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void recharge_playerNotFound_throws() {
        when(port.recharge(OP_USER, OPERATOR, PLAYER, 5000L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.recharge(OP_USER, OPERATOR, PLAYER, 5000L))
                .isInstanceOf(PlayerNotFoundException.class);
    }
}
