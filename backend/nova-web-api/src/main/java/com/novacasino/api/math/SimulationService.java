package com.novacasino.api.math;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.math.dto.SimulationAcceptedDto;
import com.novacasino.api.math.dto.SimulationStatusDto;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.math.exception.InvalidSimulationParamsException;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.api.player.GameConfigMapper;
import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Launches mass simulations and exposes their status (HU-2-BE-02). It validates the parameters,
 * inserts a {@code RUNNING} row, and hands the heavy work to {@link SimulationExecutor} (async), so
 * the request returns immediately with {@code 202}. The platform measures; it never declares the
 * target RTP.
 */
@Service
public class SimulationService {

    /** Hard cap on simulated spins (readme §4.4.4). */
    static final long MAX_SPINS = 10_000_000L;

    private final SimulationRunJpaRepository simRepo;
    private final GameConfigJpaRepository configRepo;
    private final GameJpaRepository gameRepo;
    private final GameConfigMapper configMapper;
    private final GameCompiler compiler;
    private final SimulationExecutor executor;
    private final ObjectMapper objectMapper;

    public SimulationService(final SimulationRunJpaRepository simRepo,
                             final GameConfigJpaRepository configRepo, final GameJpaRepository gameRepo,
                             final GameConfigMapper configMapper, final GameCompiler compiler,
                             final SimulationExecutor executor, final ObjectMapper objectMapper) {
        this.simRepo = simRepo;
        this.configRepo = configRepo;
        this.gameRepo = gameRepo;
        this.configMapper = configMapper;
        this.compiler = compiler;
        this.executor = executor;
        this.objectMapper = objectMapper;
    }

    /**
     * Validates the parameters, records a RUNNING run and triggers its asynchronous execution.
     * Not transactional: the RUNNING row is committed before the async worker reads it.
     */
    public SimulationAcceptedDto launch(final Long operatorId, final Long userId, final Long configId,
                                        final Long numSpins, final Long betCents) {
        if (numSpins == null || numSpins <= 0 || numSpins > MAX_SPINS) {
            throw new InvalidSimulationParamsException("numSpins must be between 1 and " + MAX_SPINS);
        }
        final GameConfigEntity config = ownedConfig(configId, operatorId);

        // betCents must be a positive multiple of the payline count (same rule as a real spin).
        final CompiledGame game = compiler.compile(config.getId(),
                configMapper.toSpec(parse(config.getConfig())));
        if (betCents == null || betCents <= 0 || betCents % game.paylineCount() != 0) {
            throw new InvalidSimulationParamsException("betCents must be a positive multiple of the payline count");
        }

        final SimulationRunEntity run = simRepo.save(
                new SimulationRunEntity(operatorId, configId, userId, numSpins, betCents));
        executor.run(run.getId(), configId, numSpins, betCents);

        return new SimulationAcceptedDto(run.getId(), run.getStatus(), run.getStartedAt(),
                "/api/v1/math/simulations/" + run.getId());
    }

    /** Returns the status (and metrics if completed) of a simulation owned by the operator. */
    @Transactional(readOnly = true)
    public SimulationStatusDto getSimulation(final Long simulationId, final Long operatorId) {
        final SimulationRunEntity run = simRepo.findById(simulationId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new SimulationNotFoundException(simulationId));
        return new SimulationStatusDto(
                run.getId(), run.getStatus(), run.getNumSpins(), run.getBetCents(),
                run.getStartedAt(), run.getCompletedAt(), run.getDurationMs(),
                run.getRtpEmpirical(), run.getRtpStdError(), run.getRtpBaseGame(), run.getRtpFreeSpins(),
                run.getHitFrequency(), run.getVolatility(), run.getMaxWinMultiplier(),
                run.getFreeSpinTriggerFreq(), run.getLongestDryStreak(),
                parseNullable(run.getPrizeDistribution()), parseNullable(run.getConvergenceSample()),
                parseNullable(run.getRtpBreakdown()), run.getErrorMessage());
    }

    // -------------------------------------------------------------------------

    /** Loads a config and checks it belongs to the operator (via its game); 404 otherwise. */
    private GameConfigEntity ownedConfig(final Long configId, final Long operatorId) {
        final GameConfigEntity config = configRepo.findById(configId)
                .orElseThrow(() -> new ConfigNotFoundException(configId));
        final GameEntity game = gameRepo.findById(config.getGameId())
                .filter(g -> g.getOperatorId().equals(operatorId))
                .orElseThrow(() -> new ConfigNotFoundException(configId));
        return config;
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final Exception e) {
            throw new IllegalStateException("Invalid config JSON in database", e);
        }
    }

    private JsonNode parseNullable(final String json) {
        if (json == null) {
            return null;
        }
        return parse(json);
    }
}
