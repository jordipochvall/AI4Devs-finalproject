package com.novacasino.infrastructure.operator;

import com.novacasino.application.operator.OperatorPlayerPort;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerSummaryDto;
import com.novacasino.common.dto.WalletDto;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import com.novacasino.infrastructure.support.Pages;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** JPA adapter for {@link OperatorPlayerPort} (HU-6): search by email + atomic recharge. */
@Component
public class OperatorPlayerJpaAdapter implements OperatorPlayerPort {

    private final UserJpaRepository userRepo;
    private final WalletJpaRepository walletRepo;
    private final WalletTransactionJpaRepository txRepo;

    public OperatorPlayerJpaAdapter(final UserJpaRepository userRepo, final WalletJpaRepository walletRepo,
                                    final WalletTransactionJpaRepository txRepo) {
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
    }

    @Override
    public PageResponse<PlayerSummaryDto> searchPlayers(final Long operatorId, final String email,
                                                        final PageRequestDto page) {
        final String filter = email == null ? "" : email;
        return Pages.of(
                userRepo.findByOperatorIdAndRoleAndEmailContainingIgnoreCase(
                        operatorId, UserRole.PLAYER, filter, PageRequest.of(page.page(), page.size())),
                this::toSummary);
    }

    @Override
    public Optional<WalletDto> recharge(final Long operatorUserId, final Long operatorId,
                                        final Long playerId, final long amountCents) {
        final UserEntity player = userRepo.findById(playerId)
                .filter(u -> u.getRole() == UserRole.PLAYER && u.getOperatorId().equals(operatorId))
                .orElse(null);
        if (player == null) {
            return Optional.empty();
        }
        final WalletEntity wallet = walletRepo.findByUserId(player.getId()).orElse(null);
        if (wallet == null) {
            return Optional.empty();
        }
        final long newBalance = wallet.getBalanceCents() + amountCents;
        wallet.setBalanceCents(newBalance);
        walletRepo.save(wallet); // optimistic lock via @Version
        txRepo.save(WalletTransactionEntity.recharge(wallet.getId(), amountCents, newBalance, operatorUserId));
        return Optional.of(new WalletDto(newBalance, wallet.getCurrency()));
    }

    private PlayerSummaryDto toSummary(final UserEntity user) {
        final WalletEntity w = walletRepo.findByUserId(user.getId()).orElse(null);
        return new PlayerSummaryDto(user.getId(), user.getEmail(), user.getLocale(), user.isActive(),
                w != null ? w.getBalanceCents() : null, w != null ? w.getCurrency() : null);
    }
}
