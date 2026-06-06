package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data repository for wallets. */
public interface WalletJpaRepository extends JpaRepository<WalletEntity, Long> {

    /** Finds the wallet belonging to a given user. */
    Optional<WalletEntity> findByUserId(Long userId);
}
