package com.novacasino.api.player;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.player.dto.PlayerRoundDto;
import com.novacasino.api.player.dto.WalletTransactionDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-14 player history (movements + rounds) in {@link PlayerHistoryService}. */
class PlayerHistoryServiceTest {

    private static final long USER_ID   = 42L;
    private static final long WALLET_ID = 7L;

    private WalletJpaRepository walletRepo;
    private WalletTransactionJpaRepository txRepo;
    private GameRoundJpaRepository roundRepo;
    private PlayerHistoryService service;

    private final Pageable pageable = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        walletRepo = mock(WalletJpaRepository.class);
        txRepo     = mock(WalletTransactionJpaRepository.class);
        roundRepo  = mock(GameRoundJpaRepository.class);
        service = new PlayerHistoryService(walletRepo, txRepo, roundRepo);
    }

    // --- AC1: wallet movements are mapped and wrapped in the standard page ---

    @Test
    void listTransactions_mapsWalletMovements() {
        when(walletRepo.findByUserId(USER_ID)).thenReturn(Optional.of(wallet(WALLET_ID)));
        final WalletTransactionEntity tx = WalletTransactionEntity.recharge(WALLET_ID, 5000L, 5000L, 1L);
        when(txRepo.findByWalletIdOrderByCreatedAtDesc(eq(WALLET_ID), any()))
                .thenReturn(new PageImpl<>(List.of(tx), pageable, 1));

        final PageResponse<WalletTransactionDto> page = service.listTransactions(USER_ID, pageable);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content()).singleElement().satisfies(dto -> {
            assertThat(dto.type()).isEqualTo("RECHARGE");
            assertThat(dto.amountCents()).isEqualTo(5000L);
            assertThat(dto.balanceAfterCents()).isEqualTo(5000L);
        });
    }

    // --- isolation/edge: a player with no wallet gets an empty page, not an error ---

    @Test
    void listTransactions_noWallet_returnsEmptyPage() {
        when(walletRepo.findByUserId(USER_ID)).thenReturn(Optional.empty());

        final PageResponse<WalletTransactionDto> page = service.listTransactions(USER_ID, pageable);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
        verifyNoInteractions(txRepo);
    }

    // --- AC2: rounds are mapped (player-scoped, no playerId leaked) ---

    @Test
    void listRounds_mapsOwnRounds() {
        final GameRoundEntity round = GameRoundEntity.baseRound(
                1L, USER_ID, 3L, 100L, 123L, 100L, 250L, 1000L, 1150L, "{}");
        when(roundRepo.findByPlayerIdOrderByCreatedAtDesc(eq(USER_ID), any()))
                .thenReturn(new PageImpl<>(List.of(round), pageable, 1));

        final PageResponse<PlayerRoundDto> page = service.listRounds(USER_ID, pageable);

        assertThat(page.content()).singleElement().satisfies(dto -> {
            assertThat(dto.gameId()).isEqualTo(3L);
            assertThat(dto.betCents()).isEqualTo(100L);
            assertThat(dto.winCents()).isEqualTo(250L);
            assertThat(dto.balancePostCents()).isEqualTo(1150L);
            assertThat(dto.freeSpin()).isFalse();
        });
        // The query is scoped to the token's user id (AC3 isolation).
        verify(roundRepo).findByPlayerIdOrderByCreatedAtDesc(eq(USER_ID), any());
    }

    // -------------------------------------------------------------------------

    private static WalletEntity wallet(final long id) {
        final WalletEntity w = new WalletEntity();
        try {
            final var f = WalletEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(w, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return w;
    }
}
