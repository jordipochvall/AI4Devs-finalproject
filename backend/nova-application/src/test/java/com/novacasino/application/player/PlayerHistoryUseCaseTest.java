package com.novacasino.application.player;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerRoundDto;
import com.novacasino.common.dto.WalletTransactionDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-14 player history in {@link PlayerHistoryUseCase} (port mocked). */
class PlayerHistoryUseCaseTest {

    private static final long USER_ID = 42L;
    private final PageRequestDto page = new PageRequestDto(0, 20);

    private PlayerHistoryPort port;
    private PlayerHistoryUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(PlayerHistoryPort.class);
        useCase = new PlayerHistoryUseCase(port);
    }

    @Test
    void listTransactions_mapsWalletMovements() {
        when(port.walletIdOf(USER_ID)).thenReturn(Optional.of(7L));
        final WalletTransactionDto tx = new WalletTransactionDto(1L, "RECHARGE", 5000L, 5000L, null, OffsetDateTime.now());
        when(port.transactionsByWallet(eq(7L), any()))
                .thenReturn(new PageResponse<>(List.of(tx), 0, 20, 1, 1));

        final PageResponse<WalletTransactionDto> result = useCase.listTransactions(USER_ID, page);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).singleElement().satisfies(dto ->
                assertThat(dto.type()).isEqualTo("RECHARGE"));
    }

    @Test
    void listTransactions_noWallet_returnsEmptyPage() {
        when(port.walletIdOf(USER_ID)).thenReturn(Optional.empty());

        final PageResponse<WalletTransactionDto> result = useCase.listTransactions(USER_ID, page);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        verify(port, never()).transactionsByWallet(any(), any());
    }

    @Test
    void listRounds_delegatesToPort() {
        final PlayerRoundDto round = new PlayerRoundDto(9L, 3L, 100L, 250L, 1150L, false, OffsetDateTime.now());
        when(port.roundsByPlayer(eq(USER_ID), any()))
                .thenReturn(new PageResponse<>(List.of(round), 0, 20, 1, 1));

        final PageResponse<PlayerRoundDto> result = useCase.listRounds(USER_ID, page);

        assertThat(result.content()).singleElement().satisfies(dto -> {
            assertThat(dto.gameId()).isEqualTo(3L);
            assertThat(dto.winCents()).isEqualTo(250L);
        });
    }
}
