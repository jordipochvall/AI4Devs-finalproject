package com.novacasino.api.operator;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.operator.dto.PlayerSummaryDto;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Operator player management: paginated search and wallet recharge.
 * The recharge idempotency is provided by {@code IdempotencyService} (the controller wraps it).
 */
@Service
public class OperatorPlayerService {

    private final UserJpaRepository userRepo;
    private final WalletJpaRepository walletRepo;
    private final WalletTransactionJpaRepository txRepo;

    public OperatorPlayerService(final UserJpaRepository userRepo,
                                 final WalletJpaRepository walletRepo,
                                 final WalletTransactionJpaRepository txRepo) {
        this.userRepo   = userRepo;
        this.walletRepo = walletRepo;
        this.txRepo     = txRepo;
    }

    /** Paginated search of players by partial email within the operator, including balance. */
    @Transactional(readOnly = true)
    public PageResponse<PlayerSummaryDto> searchPlayers(final Long operatorId, final String email,
                                                        final Pageable pageable) {
        final String filter = email == null ? "" : email;
        return PageResponse.of(
                userRepo.findByOperatorIdAndRoleAndEmailContainingIgnoreCase(
                        operatorId, UserRole.PLAYER, filter, pageable),
                this::toSummary);
    }

    /** Recharges the player's balance and records the RECHARGE movement with the operator. */
    @Transactional
    public WalletDto recharge(final Long operatorUserId, final Long operatorId,
                              final Long playerId, final Long amountCents) {
        if (amountCents == null || amountCents <= 0) {
            throw new InvalidAmountException();
        }

        final UserEntity player = userRepo.findById(playerId)
                .filter(u -> u.getRole() == UserRole.PLAYER && u.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        final WalletEntity wallet = walletRepo.findByUserId(player.getId())
                .orElseThrow(() -> new PlayerNotFoundException(playerId));

        final long newBalance = wallet.getBalanceCents() + amountCents;
        wallet.setBalanceCents(newBalance);
        walletRepo.save(wallet);   // optimistic lock via @Version

        txRepo.save(WalletTransactionEntity.recharge(
                wallet.getId(), amountCents, newBalance, operatorUserId));

        return new WalletDto(newBalance, wallet.getCurrency());
    }

    // -------------------------------------------------------------------------

    private PlayerSummaryDto toSummary(final UserEntity user) {
        final WalletEntity w = walletRepo.findByUserId(user.getId()).orElse(null);
        return new PlayerSummaryDto(
                user.getId(),
                user.getEmail(),
                user.getLocale(),
                user.isActive(),
                w != null ? w.getBalanceCents() : null,
                w != null ? w.getCurrency() : null);
    }
}
