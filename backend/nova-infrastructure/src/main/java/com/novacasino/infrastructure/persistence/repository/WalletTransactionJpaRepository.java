package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for the wallet ledger (append-only). */
public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransactionEntity, Long> {
}
