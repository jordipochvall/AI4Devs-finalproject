package com.novacasino.api.math;

import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Startup reconciliation of orphaned simulations (T2). A mass simulation runs on an in-memory
 * {@code @Async} worker after its {@code RUNNING} row is committed; if the JVM dies before the worker
 * writes {@code COMPLETED}/{@code FAILED}, the row would stay {@code RUNNING} forever and the client's
 * polling never resolves. On a single-node deployment (readme §S3) any {@code RUNNING} run present at
 * startup is therefore orphaned, so we mark it {@code FAILED}.
 */
@Component
public class SimulationReconciler implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SimulationReconciler.class);

    private final SimulationRunJpaRepository simRepo;

    public SimulationReconciler(final SimulationRunJpaRepository simRepo) {
        this.simRepo = simRepo;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        final int orphaned = simRepo.failRunningSimulations(
                "Marked failed on startup: the simulation worker did not survive a restart (orphaned RUNNING).",
                OffsetDateTime.now());
        if (orphaned > 0) {
            log.warn("Reconciled {} orphaned RUNNING simulation(s) to FAILED at startup.", orphaned);
        }
    }
}
