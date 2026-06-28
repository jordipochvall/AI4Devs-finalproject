package com.novacasino.api.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.player.GameConfigMapper;
import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import com.novacasino.simulator.SimulationResult;
import com.novacasino.simulator.SimulationRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

/**
 * Runs a simulation asynchronously (HU-2-BE-02): compiles the config, executes the
 * {@link SimulationRunner} off the request thread and persists the aggregated metrics, flipping the
 * run to {@code COMPLETED} (or {@code FAILED} with the error). Being a separate bean, the
 * {@code @Async} proxy applies when invoked from {@link SimulationService}.
 */
@Component
public class SimulationExecutor {

    private static final Logger log = LoggerFactory.getLogger(SimulationExecutor.class);

    private final SimulationRunJpaRepository simRepo;
    private final GameConfigJpaRepository configRepo;
    private final GameConfigMapper configMapper;
    private final GameCompiler compiler;
    private final SimulationRunner runner;
    private final ObjectMapper objectMapper;

    public SimulationExecutor(final SimulationRunJpaRepository simRepo,
                              final GameConfigJpaRepository configRepo,
                              final GameConfigMapper configMapper, final GameCompiler compiler,
                              final SimulationRunner runner, final ObjectMapper objectMapper) {
        this.simRepo = simRepo;
        this.configRepo = configRepo;
        this.configMapper = configMapper;
        this.compiler = compiler;
        this.runner = runner;
        this.objectMapper = objectMapper;
    }

    /** Executes the run in a background thread; never throws to the caller. */
    @Async
    public void run(final Long simulationId, final Long configId, final long numSpins, final long betCents) {
        try {
            final GameConfigEntity config = configRepo.findById(configId)
                    .orElseThrow(() -> new IllegalStateException("Config gone: " + configId));
            final CompiledGame game = compiler.compile(config.getId(),
                    configMapper.toSpec(objectMapper.readTree(config.getConfig())));
            final SimulationResult result = runner.run(game, numSpins, betCents, simulationId);
            complete(simulationId, result);
        } catch (final Exception e) {
            log.error("Simulation {} failed", simulationId, e);
            fail(simulationId, e.getMessage());
        }
    }

    void complete(final Long simulationId, final SimulationResult r) {
        final SimulationRunEntity run = simRepo.findById(simulationId).orElseThrow();
        run.setRtpEmpirical(scale(r.rtp(), 4));
        run.setRtpStdError(scale(r.rtpStdError(), 6));
        run.setRtpBaseGame(scale(r.rtpBaseGame(), 4));
        run.setRtpFreeSpins(scale(r.rtpFreeSpins(), 4));
        run.setHitFrequency(scale(r.hitFrequency(), 4));
        run.setVolatility(scale(r.volatility(), 2));
        run.setMaxWinMultiplier(scale(r.maxWinMultiplier(), 2));
        run.setFreeSpinTriggerFreq(scale(r.freeSpinTriggerFrequency(), 4));
        run.setLongestDryStreak((int) Math.min(Integer.MAX_VALUE, r.longestDryStreak()));
        run.setPrizeDistribution(toJson(r.prizeDistribution()));
        run.setConvergenceSample(toJson(r.convergenceSample()));
        run.setRtpBreakdown(toJson(r.rtpBreakdown()));
        run.setDurationMs(r.durationMs());
        run.setCompletedAt(OffsetDateTime.now());
        run.setStatus(SimulationRunEntity.COMPLETED);
        simRepo.save(run);
        log.info("Simulation completed: id={}, spins={}, rtpEmpirical={}, durationMs={}",
                simulationId, run.getNumSpins(), run.getRtpEmpirical(), run.getDurationMs());
    }

    void fail(final Long simulationId, final String message) {
        simRepo.findById(simulationId).ifPresent(run -> {
            run.setStatus(SimulationRunEntity.FAILED);
            run.setErrorMessage(message != null ? message : "Simulation failed");
            run.setCompletedAt(OffsetDateTime.now());
            simRepo.save(run);
        });
    }

    /** Rounds a metric to the column's scale so it fits the NUMERIC definition (§3.2.9). */
    private static Double scale(final double value, final int scale) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private String toJson(final Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (final Exception e) {
            throw new IllegalStateException("Could not serialize simulation metrics", e);
        }
    }
}
