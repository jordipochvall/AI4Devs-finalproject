package com.novacasino.api.player;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.player.dto.PlayerRoundDto;
import com.novacasino.api.player.dto.WalletTransactionDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletTransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only player history (HU-14): the player's own wallet movements and rounds, paginated and
 * ordered newest-first. Strictly scoped to the authenticated player's id (its wallet for movements,
 * its player_id for rounds) — never another player's data.
 */
@Service
public class PlayerHistoryService {

    private final WalletJpaRepository walletRepo;
    private final WalletTransactionJpaRepository txRepo;
    private final GameRoundJpaRepository roundRepo;

    public PlayerHistoryService(final WalletJpaRepository walletRepo,
                                final WalletTransactionJpaRepository txRepo,
                                final GameRoundJpaRepository roundRepo) {
        this.walletRepo = walletRepo;
        this.txRepo     = txRepo;
        this.roundRepo  = roundRepo;
    }

    /** The player's wallet ledger movements, newest first (AC1). */
    @Transactional(readOnly = true)
    public PageResponse<WalletTransactionDto> listTransactions(final Long userId, final Pageable pageable) {
        return walletRepo.findByUserId(userId)
                .map(wallet -> PageResponse.of(
                        txRepo.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable), this::toTxDto))
                .orElseGet(() -> PageResponse.of(Page.<WalletTransactionEntity>empty(pageable), this::toTxDto));
    }

    /** The player's own rounds, newest first (AC2). */
    @Transactional(readOnly = true)
    public PageResponse<PlayerRoundDto> listRounds(final Long userId, final Pageable pageable) {
        return PageResponse.of(roundRepo.findByPlayerIdOrderByCreatedAtDesc(userId, pageable), this::toRoundDto);
    }

    // -------------------------------------------------------------------------

    private WalletTransactionDto toTxDto(final WalletTransactionEntity tx) {
        return new WalletTransactionDto(tx.getId(), tx.getType().name(), tx.getAmountCents(),
                tx.getBalanceAfterCents(), tx.getGameRoundId(), tx.getCreatedAt());
    }

    private PlayerRoundDto toRoundDto(final GameRoundEntity r) {
        return new PlayerRoundDto(r.getId(), r.getGameId(), r.getBetCents(), r.getWinCents(),
                r.getBalancePostCents(), r.isFreeSpin(), r.getCreatedAt());
    }
}
