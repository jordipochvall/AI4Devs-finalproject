package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

/** Spring Data repository for mass-simulation runs. */
public interface SimulationRunJpaRepository extends JpaRepository<SimulationRunEntity, Long> {

    /**
     * Marks every still-{@code RUNNING} run as {@code FAILED} (startup reconciliation, T2): on a
     * single-node deployment any run left RUNNING after a restart is orphaned — its in-memory async
     * worker did not survive — so it would otherwise stay RUNNING forever.
     *
     * @return the number of runs reconciled
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE SimulationRunEntity s
               SET s.status = 'FAILED', s.errorMessage = :message, s.completedAt = :now
             WHERE s.status = 'RUNNING'""")
    int failRunningSimulations(@Param("message") String message, @Param("now") OffsetDateTime now);

    /** Number of runs currently in a given status (HU-37: caps concurrent RUNNING simulations). */
    long countByStatus(String status);

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
