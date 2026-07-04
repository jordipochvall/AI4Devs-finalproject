package com.novacasino.api.player;

import com.novacasino.application.player.ResponsibleGamingUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.idempotency.IdempotencyService;
import com.novacasino.common.dto.SpinResultDto;
import com.novacasino.api.player.exception.ConcurrentSpinException;
import com.novacasino.api.player.exception.InsufficientBalanceException;
import com.novacasino.api.player.exception.InvalidBetException;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.rng.RngEngine;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpinService} with mocked repositories and a fixed RNG. Covers prize/balance
 * math (AC1/AC2), bet validation (AC4) and the optimistic-lock retry giving up with a 409 (AC8).
 * Idempotency replay/conflict and full transactionality are covered by the integration tests.
 */
class SpinServiceTest {

    private static final long USER_ID = 7L;
    private static final long OPERATOR_ID = 1L;
    private static final long GAME_ID = 3L;
    private static final long CONFIG_ID = 11L;
    private static final UUID KEY = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");

    /** 3x3, single middle payline; the middle row is always A,A,A (A pays 5 for 3). */
    private static final String CONFIG_JSON = """
            {"grid":{"cols":3,"rows":3},
             "symbols":[{"id":"A","kind":"REGULAR"},{"id":"B","kind":"REGULAR"}],
             "reels":[["B","A","B"],["B","A","B"],["B","A","B"]],
             "paylines":[[1,1,1]],
             "paytable":[{"symbol":"A","payouts":{"3":5}},{"symbol":"B","payouts":{"3":2}}]}""";

    private GameJpaRepository gameRepo;
    private GameConfigJpaRepository configRepo;
    private WalletJpaRepository walletRepo;
    private WalletTransactionJpaRepository txRepo;
    private GameRoundJpaRepository roundRepo;
    private RngFactory rngFactory;
    private IdempotencyService idempotency;
    private ResponsibleGamingUseCase responsibleGaming;
    private JackpotService jackpotService;
    private SpinService service;

    @BeforeEach
    void setUp() {
        gameRepo = mock(GameJpaRepository.class);
        configRepo = mock(GameConfigJpaRepository.class);
        walletRepo = mock(WalletJpaRepository.class);
        txRepo = mock(WalletTransactionJpaRepository.class);
        roundRepo = mock(GameRoundJpaRepository.class);
        rngFactory = mock(RngFactory.class);
        idempotency = mock(IdempotencyService.class);
        responsibleGaming = mock(ResponsibleGamingUseCase.class); // no-op gate by default
        jackpotService = mock(JackpotService.class);
        when(jackpotService.findPool(anyLong())).thenReturn(java.util.Optional.empty()); // no jackpot by default

        service = new SpinService(gameRepo, configRepo, walletRepo, txRepo, roundRepo,
                new GameConfigMapper(), new GameCompiler(), rngFactory, idempotency,
                responsibleGaming, jackpotService, new ObjectMapper());

        // Run the supplied operation directly (idempotency itself is covered by the IT suite).
        when(idempotency.execute(anyLong(), anyString(), any(), any(), any(), any()))
                .thenAnswer(inv -> ((Supplier<?>) inv.getArgument(5)).get());
        // Fixed RNG: every reel stops at 0 → window equals the reels → middle row A,A,A.
        final RngEngine zeroRng = bound -> 0;
        when(rngFactory.create(anyLong())).thenReturn(zeroRng);
    }

    @Test
    void resolvesSpinUpdatesBalanceAndRecordsLedger() {
        stubGameAndConfig(100L, 1000L, 100L);
        final WalletEntity wallet = wallet(100_000L);
        when(walletRepo.findByUserId(USER_ID)).thenReturn(Optional.of(wallet));
        when(roundRepo.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 900L));

        final SpinResultDto result = service.spin(USER_ID, OPERATOR_ID, GAME_ID, KEY, 100L, "EUR");

        assertThat(result.betCents()).isEqualTo(100L);
        assertThat(result.lineBetCents()).isEqualTo(100L);
        assertThat(result.winCents()).isEqualTo(500L);            // A pays 5 × lineBet(100)
        assertThat(result.balancePreCents()).isEqualTo(100_000L);
        assertThat(result.balancePostCents()).isEqualTo(100_400L); // 100000 - 100 + 500
        assertThat(result.winningPaylines()).singleElement()
                .satisfies(line -> {
                    assertThat(line.symbol()).isEqualTo("A");
                    assertThat(line.count()).isEqualTo(3);
                    assertThat(line.winCents()).isEqualTo(500L);
                });
        assertThat(result.freeSpins().triggered()).isFalse();

        assertThat(wallet.getBalanceCents()).isEqualTo(100_400L);
        verify(walletRepo).save(wallet);
        verify(roundRepo, times(1)).save(any());                  // one base round, no free spins
        verify(txRepo, times(2)).save(any(WalletTransactionEntity.class)); // BET + WIN
    }

    @Test
    void rejectsBetOutOfRange() {
        stubGameAndConfig(100L, 1000L, 100L);
        assertThatThrownBy(() -> service.spin(USER_ID, OPERATOR_ID, GAME_ID, KEY, 5_000L, "EUR"))
                .isInstanceOf(InvalidBetException.class);
        verifyNoInteractions(walletRepo);
    }

    @Test
    void rejectsBetNotMultipleOfStep() {
        stubGameAndConfig(100L, 1000L, 100L);
        assertThatThrownBy(() -> service.spin(USER_ID, OPERATOR_ID, GAME_ID, KEY, 150L, "EUR"))
                .isInstanceOf(InvalidBetException.class);
    }

    @Test
    void rejectsInsufficientBalance() {
        stubGameAndConfig(100L, 1000L, 100L);
        when(walletRepo.findByUserId(USER_ID)).thenReturn(Optional.of(wallet(50L)));
        assertThatThrownBy(() -> service.spin(USER_ID, OPERATOR_ID, GAME_ID, KEY, 100L, "EUR"))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(walletRepo, never()).save(any());
    }

    @Test
    void givesUpWithConflictAfterRepeatedOptimisticLockFailures() {
        stubGameAndConfig(100L, 1000L, 100L); // the read-only setup runs before the retry loop now
        when(idempotency.execute(anyLong(), eq("spin"), any(), any(), any(), any()))
                .thenThrow(new OptimisticLockingFailureException("conflict"));

        assertThatThrownBy(() -> service.spin(USER_ID, OPERATOR_ID, GAME_ID, KEY, 100L, "EUR"))
                .isInstanceOf(ConcurrentSpinException.class);
        verify(idempotency, times(3)).execute(anyLong(), eq("spin"), any(), any(), any(), any());
    }

    // -------------------------------------------------------------------------

    private void stubGameAndConfig(final long min, final long max, final long step) {
        final GameEntity game = mock(GameEntity.class);
        when(game.getActiveConfigId()).thenReturn(CONFIG_ID);
        when(game.getMinBetCents()).thenReturn(min);
        when(game.getMaxBetCents()).thenReturn(max);
        when(game.getBetStepCents()).thenReturn(step);
        when(gameRepo.findByIdAndActiveTrue(GAME_ID)).thenReturn(Optional.of(game));

        final GameConfigEntity config = mock(GameConfigEntity.class);
        when(config.getId()).thenReturn(CONFIG_ID);
        when(config.getConfig()).thenReturn(CONFIG_JSON);
        when(configRepo.findById(CONFIG_ID)).thenReturn(Optional.of(config));
    }

    private static WalletEntity wallet(final long balanceCents) {
        final WalletEntity w = new WalletEntity();
        w.setUserId(USER_ID);
        w.setBalanceCents(balanceCents);
        w.setCurrency("EUR");
        withId(w, 50L);
        return w;
    }

    private static <T> T withId(final T entity, final long id) {
        try {
            final var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
}
