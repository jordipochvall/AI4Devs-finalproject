package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data repository for mass-simulation runs. */
public interface SimulationRunJpaRepository extends JpaRepository<SimulationRunEntity, Long> {

    /**
     * Operator's simulation history (HU-18), newest first, optionally filtered by config or by game
     * (a game maps to its config versions). Null filters are ignored.
     */
    @Query("""
            SELECT s FROM SimulationRunEntity s
            WHERE s.operatorId = :operatorId
              AND (:configId IS NULL OR s.gameConfigId = :configId)
              AND (:gameId IS NULL OR s.gameConfigId IN
                    (SELECT c.id FROM GameConfigEntity c WHERE c.gameId = :gameId))
            ORDER BY s.startedAt DESC""")
    Page<SimulationRunEntity> search(@Param("operatorId") Long operatorId,
                                     @Param("configId") Long configId,
                                     @Param("gameId") Long gameId,
                                     Pageable pageable);
}
