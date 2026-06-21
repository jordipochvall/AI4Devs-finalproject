package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameCommercialAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Append-only commercial-change audit (HU-15). Entries are queryable by game and operator. */
public interface GameCommercialAuditJpaRepository
        extends JpaRepository<GameCommercialAuditEntity, Long> {

    /** Audit entries for a game (within its operator), newest first. */
    List<GameCommercialAuditEntity> findByGameIdAndOperatorIdOrderByCreatedAtDesc(Long gameId, Long operatorId);
}
