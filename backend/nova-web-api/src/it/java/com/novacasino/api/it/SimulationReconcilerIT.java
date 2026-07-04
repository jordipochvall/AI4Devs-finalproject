package com.novacasino.api.it;

import com.novacasino.api.math.SimulationReconciler;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Startup reconciliation of orphaned simulations (T2): a run left {@code RUNNING} after a crash must be
 * marked {@code FAILED} so its polling resolves instead of hanging forever.
 */
class SimulationReconcilerIT extends AbstractIntegrationTest {

    @Autowired
    private SimulationRunJpaRepository simRepo;

    @Autowired
    private SimulationReconciler reconciler;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void reconcile_marksOrphanedRunningRunAsFailed() {
        final Long mathUserId = jdbc.queryForObject(
                "SELECT id FROM users WHERE role = 'MATH_ANALYST' LIMIT 1", Long.class);
        final Long operatorId = jdbc.queryForObject(
                "SELECT operator_id FROM users WHERE id = ?", Long.class, mathUserId);
        final Long configId = jdbc.queryForObject("SELECT id FROM game_configs LIMIT 1", Long.class);

        // Simulate an orphaned run (RUNNING, its async worker gone).
        final SimulationRunEntity run = simRepo.save(
                new SimulationRunEntity(operatorId, configId, mathUserId, 1_000L, 100L));
        assertThat(run.getStatus()).isEqualTo(SimulationRunEntity.RUNNING);

        reconciler.run(null);

        final SimulationRunEntity reloaded = simRepo.findById(run.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(SimulationRunEntity.FAILED);
        assertThat(reloaded.getErrorMessage()).contains("orphaned");
        assertThat(reloaded.getCompletedAt()).isNotNull();
    }
}
