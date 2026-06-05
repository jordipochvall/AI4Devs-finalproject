package com.novacasino.api.player;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PlayerCatalogServiceTest {

    private GameJpaRepository gameRepo;
    private GameConfigJpaRepository configRepo;
    private WalletJpaRepository walletRepo;
    private PlayerCatalogService service;

    @BeforeEach
    void setUp() {
        gameRepo   = mock(GameJpaRepository.class);
        configRepo = mock(GameConfigJpaRepository.class);
        walletRepo = mock(WalletJpaRepository.class);
        service    = new PlayerCatalogService(gameRepo, configRepo, walletRepo, new ObjectMapper());
    }

    @Test
    void getActiveGame_notFound_throws() {
        when(gameRepo.findByIdAndActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getActiveGame(99L))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void getWallet_returnsBalanceOfUser() {
        WalletEntity w = new WalletEntity();
        w.setUserId(7L);
        w.setBalanceCents(100_000L);
        w.setCurrency("EUR");
        when(walletRepo.findByUserId(7L)).thenReturn(Optional.of(w));

        WalletDto dto = service.getWallet(7L);

        assertThat(dto.balanceCents()).isEqualTo(100_000L);
        assertThat(dto.currency()).isEqualTo("EUR");
    }

    @Test
    void getWallet_missing_throws() {
        when(walletRepo.findByUserId(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getWallet(7L))
                .isInstanceOf(IllegalStateException.class);
    }
}
