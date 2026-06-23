package com.novacasino.application.player;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.common.dto.GameDetailDto;
import com.novacasino.common.dto.WalletDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/** Unit tests for HU-5 player catalogue in {@link PlayerCatalogUseCase} (port mocked). */
class PlayerCatalogUseCaseTest {

    private PlayerCatalogPort port;
    private PlayerCatalogUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(PlayerCatalogPort.class);
        useCase = new PlayerCatalogUseCase(port);
    }

    @Test
    void getActiveGame_present_returnsDetail() {
        final GameDetailDto detail = new GameDetailDto(3L, "Frutas", "FRUITS", "", 100, 1000, 100, null, null);
        when(port.activeGame(3L)).thenReturn(Optional.of(detail));
        assertThat(useCase.getActiveGame(3L).name()).isEqualTo("Frutas");
    }

    @Test
    void getActiveGame_missing_throwsNotFound() {
        when(port.activeGame(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.getActiveGame(99L)).isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void getWallet_present_returnsBalance() {
        when(port.walletOf(42L)).thenReturn(Optional.of(new WalletDto(100_000L, "EUR")));
        assertThat(useCase.getWallet(42L).balanceCents()).isEqualTo(100_000L);
    }

    @Test
    void getWallet_missing_throws() {
        when(port.walletOf(42L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.getWallet(42L)).isInstanceOf(IllegalStateException.class);
    }
}
