package com.novacasino.infrastructure.player;

import com.novacasino.application.player.PlayerHistoryPort;
import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerRoundDto;
import com.novacasino.common.dto.WalletTransactionDto;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import com.novacasino.infrastructure.support.Pages;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** JPA adapter for {@link PlayerHistoryPort} (HU-14): wallet movements + own rounds, paginated. */
@Component
public class PlayerHistoryJpaAdapter implements PlayerHistoryPort {

    private final WalletJpaRepository walletRepo;
    private final WalletTransactionJpaRepository txRepo;
    private final GameRoundJpaRepository roundRepo;

    public PlayerHistoryJpaAdapter(final WalletJpaRepository walletRepo,
                                   final WalletTransactionJpaRepository txRepo,
                                   final GameRoundJpaRepository roundRepo) {
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
        this.roundRepo = roundRepo;
    }

    @Override
    public Optional<Long> walletIdOf(final Long userId) {
        return walletRepo.findByUserId(userId).map(w -> w.getId());
    }

    @Override
    public PageResponse<WalletTransactionDto> transactionsByWallet(final Long walletId, final PageRequestDto page) {
        return Pages.of(
                txRepo.findByWalletIdOrderByCreatedAtDesc(walletId, PageRequest.of(page.page(), page.size())),
                tx -> new WalletTransactionDto(tx.getId(), tx.getType().name(), tx.getAmountCents(),
                        tx.getBalanceAfterCents(), tx.getGameRoundId(), tx.getCreatedAt()));
    }

    @Override
    public PageResponse<PlayerRoundDto> roundsByPlayer(final Long userId, final PageRequestDto page) {
        return Pages.of(
                roundRepo.findByPlayerIdOrderByCreatedAtDesc(userId, PageRequest.of(page.page(), page.size())),
                r -> new PlayerRoundDto(r.getId(), r.getGameId(), r.getBetCents(), r.getWinCents(),
                        r.getBalancePostCents(), r.isFreeSpin(), r.getCreatedAt()));
    }
}
