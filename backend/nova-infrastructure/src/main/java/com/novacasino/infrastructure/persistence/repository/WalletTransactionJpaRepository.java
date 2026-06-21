package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for the wallet ledger (append-only). */
public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransactionEntity, Long> {

    /** A player's ledger movements (by wallet), newest first (HU-14). */
    Page<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
}
