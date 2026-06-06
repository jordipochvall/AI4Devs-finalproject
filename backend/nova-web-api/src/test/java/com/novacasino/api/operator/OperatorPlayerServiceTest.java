package com.novacasino.api.operator;

import com.novacasino.api.operator.exception.InvalidAmountException;
import com.novacasino.api.operator.exception.PlayerNotFoundException;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for {@link OperatorPlayerService} (recharge rules) with mocked repositories. */
class OperatorPlayerServiceTest {

    private UserJpaRepository userRepo;
    private WalletJpaRepository walletRepo;
    private WalletTransactionJpaRepository txRepo;
    private OperatorPlayerService service;

    private static final long OPERATOR_ID = 1L;
    private static final long OPERATOR_USER_ID = 10L;

    @BeforeEach
    void setUp() {
        userRepo   = mock(UserJpaRepository.class);
        walletRepo = mock(WalletJpaRepository.class);
        txRepo     = mock(WalletTransactionJpaRepository.class);
        service    = new OperatorPlayerService(userRepo, walletRepo, txRepo);
    }

    // --- AC3: amount <= 0 → 422 ---

    @Test
    void recharge_nonPositiveAmount_throws() {
        assertThatThrownBy(() -> service.recharge(OPERATOR_USER_ID, OPERATOR_ID, 5L, 0L))
                .isInstanceOf(InvalidAmountException.class);
        assertThatThrownBy(() -> service.recharge(OPERATOR_USER_ID, OPERATOR_ID, 5L, -100L))
                .isInstanceOf(InvalidAmountException.class);
        verifyNoInteractions(walletRepo, txRepo);
    }

    // --- AC3: player not found → 404 ---

    @Test
    void recharge_playerNotFound_throws() {
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.recharge(OPERATOR_USER_ID, OPERATOR_ID, 99L, 5000L))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    // --- AC2: valid recharge increments balance and records the movement ---

    @Test
    void recharge_valid_incrementsBalanceAndRecordsTransaction() {
        final UserEntity player = player(5L, OPERATOR_ID);
        final WalletEntity wallet = new WalletEntity();
        wallet.setUserId(5L);
        wallet.setBalanceCents(100_000L);
        wallet.setCurrency("EUR");

        when(userRepo.findById(5L)).thenReturn(Optional.of(player));
        when(walletRepo.findByUserId(5L)).thenReturn(Optional.of(wallet));

        final WalletDto result = service.recharge(OPERATOR_USER_ID, OPERATOR_ID, 5L, 5_000L);

        assertThat(result.balanceCents()).isEqualTo(105_000L);
        assertThat(wallet.getBalanceCents()).isEqualTo(105_000L);
        verify(walletRepo).save(wallet);
        verify(txRepo).save(any(WalletTransactionEntity.class));
    }

    // --- player of another operator → 404 ---

    @Test
    void recharge_playerOfAnotherOperator_throws() {
        when(userRepo.findById(5L)).thenReturn(Optional.of(player(5L, 999L /* another operator */)));
        assertThatThrownBy(() -> service.recharge(OPERATOR_USER_ID, OPERATOR_ID, 5L, 5000L))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    // -------------------------------------------------------------------------

    private static UserEntity player(final long id, final long operatorId) {
        final UserEntity u = new UserEntity();
        u.setOperatorId(operatorId);
        u.setRole(UserRole.PLAYER);
        setId(u, id);
        return u;
    }

    private static void setId(final Object entity, final long id) {
        try {
            final var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
