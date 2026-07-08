package com.novacasino.api.math;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.player.GameConfigMapper;
import com.novacasino.application.math.SimulationLaunchPort;
import com.novacasino.common.dto.SimulationAcceptedDto;
import com.novacasino.common.dto.SimulationStatusDto;
import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adapter for {@link SimulationLaunchPort} (HU-2). Lives in the web module because it orchestrates the
 * async {@link SimulationExecutor} and the {@link GameConfigMapper}, both web collaborators; it keeps
 * {@code nova-application} free of any dependency on them.
 */
@Component
public class SimulationLaunchAdapter implements SimulationLaunchPort {

    private final SimulationRunJpaRepository simRepo;
    private final GameConfigJpaRepository configRepo;
    private final GameJpaRepository gameRepo;
    private final GameConfigMapper configMapper;
    private final GameCompiler compiler;
    private final SimulationExecutor executor;
    private final ObjectMapper objectMapper;

    public SimulationLaunchAdapter(final SimulationRunJpaRepository simRepo,
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

    @Override
    @Transactional(readOnly = true)
    public Optional<Integer> ownedConfigPaylineCount(final Long configId, final Long operatorId) {
        final GameConfigEntity config = configRepo.findById(configId).orElse(null);
        if (config == null) {
            return Optional.empty();
        }
        final boolean owned = gameRepo.findById(config.getGameId())
                .filter(g -> g.getOperatorId().equals(operatorId)).isPresent();
        if (!owned) {
            return Optional.empty();
        }
        final CompiledGame game = compiler.compile(config.getId(),
                configMapper.toSpec(parse(config.getConfig())));
        return Optional.of(game.paylineCount());
    }

    @Override
    @Transactional(readOnly = true)
    public long countRunning() {
        return simRepo.countByStatus(SimulationRunEntity.RUNNING);
    }

    @Override
    public SimulationAcceptedDto createAndLaunch(final Long operatorId, final Long userId, final Long configId,
                                                 final long numSpins, final long betCents) {
        final SimulationRunEntity run = simRepo.save(
                new SimulationRunEntity(operatorId, configId, userId, numSpins, betCents));
        executor.run(run.getId(), configId, numSpins, betCents);
        return new SimulationAcceptedDto(run.getId(), run.getStatus(), run.getStartedAt(),
                "/api/v1/math/simulations/" + run.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SimulationStatusDto> getSimulation(final Long simulationId, final Long operatorId) {
        return simRepo.findById(simulationId)
                .filter(r -> r.getOperatorId().equals(operatorId))
                .map(run -> new SimulationStatusDto(
                        run.getId(), run.getStatus(), run.getNumSpins(), run.getBetCents(),
                        run.getStartedAt(), run.getCompletedAt(), run.getDurationMs(),
                        run.getRtpEmpirical(), run.getRtpStdError(), run.getRtpBaseGame(), run.getRtpFreeSpins(),
                        run.getHitFrequency(), run.getVolatility(), run.getMaxWinMultiplier(),
                        run.getFreeSpinTriggerFreq(), run.getLongestDryStreak(),
                        parseNullable(run.getPrizeDistribution()), parseNullable(run.getConvergenceSample()),
                        parseNullable(run.getRtpBreakdown()), run.getErrorMessage()));
    }

    private JsonNode parse(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (final Exception e) {
            throw new IllegalStateException("Invalid config JSON in database", e);
        }
    }

    private JsonNode parseNullable(final String json) {
        return json == null ? null : parse(json);
    }
}
