package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Spring Data repository for the auditable game-round log (append-only). Audit search uses
 * {@link JpaSpecificationExecutor} so only the filters actually present are added to the SQL (avoids
 * null-typed parameters); ordering/paging come from the {@code Pageable} (the service defaults to
 * {@code created_at DESC}, served by the {@code idx_game_rounds_*_created} indexes — readme §3.2.8).
 */
public interface GameRoundJpaRepository extends JpaRepository<GameRoundEntity, Long>,
        JpaSpecificationExecutor<GameRoundEntity> {

    /** Free-spin child rounds of a triggering round, in chronological order (for replay). */
    java.util.List<GameRoundEntity> findByTriggeringRoundIdOrderByIdAsc(Long triggeringRoundId);
}
