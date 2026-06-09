package com.novacasino.api.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.math.dto.SimulationAcceptedDto;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.math.exception.InvalidSimulationParamsException;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.api.player.GameConfigMapper;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for {@link SimulationService}: parameter validation (AC2), launch wiring and lookup. */
class SimulationServiceTest {

    private static final long OPERATOR_ID = 1L;
    private static final long USER_ID = 9L;
    private static final long CONFIG_ID = 15L;
    private static final long GAME_ID = 3L;

    /** 3x3, single payline → any positive bet is a valid multiple. */
    private static final String CONFIG_1_LINE = """
            {"grid":{"cols":3,"rows":3},
             "symbols":[{"id":"A","kind":"REGULAR"}],
             "reels":[["A","A","A"],["A","A","A"],["A","A","A"]],
             "paylines":[[1,1,1]],
             "paytable":[{"symbol":"A","payouts":{"3":5}}]}""";

    private SimulationRunJpaRepository simRepo;
    private GameConfigJpaRepository configRepo;
    private GameJpaRepository gameRepo;
    private SimulationExecutor executor;
    private SimulationService service;

    @BeforeEach
    void setUp() {
        simRepo = mock(SimulationRunJpaRepository.class);
        configRepo = mock(GameConfigJpaRepository.class);
        gameRepo = mock(GameJpaRepository.class);
        executor = mock(SimulationExecutor.class);
        service = new SimulationService(simRepo, configRepo, gameRepo,
                new GameConfigMapper(), new GameCompiler(), executor, new ObjectMapper());
    }

    @Test
    void launch_outOfRangeSpins_throws() {
        assertThatThrownBy(() -> service.launch(OPERATOR_ID, USER_ID, CONFIG_ID, 0L, 100L))
                .isInstanceOf(InvalidSimulationParamsException.class);
        assertThatThrownBy(() -> service.launch(OPERATOR_ID, USER_ID, CONFIG_ID, 10_000_001L, 100L))
                .isInstanceOf(InvalidSimulationParamsException.class);
        verifyNoInteractions(executor);
    }

    @Test
    void launch_configNotFound_throws() {
        when(configRepo.findById(CONFIG_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.launch(OPERATOR_ID, USER_ID, CONFIG_ID, 1000L, 100L))
                .isInstanceOf(ConfigNotFoundException.class);
    }

    @Test
    void launch_valid_savesRunningAndTriggersExecutor() {
        stubOwnedConfig();
        when(simRepo.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 308L));

        final SimulationAcceptedDto dto = service.launch(OPERATOR_ID, USER_ID, CONFIG_ID, 1_000_000L, 100L);

        assertThat(dto.simulationId()).isEqualTo(308L);
        assertThat(dto.status()).isEqualTo(SimulationRunEntity.RUNNING);
        assertThat(dto.pollUrl()).isEqualTo("/api/v1/math/simulations/308");
        verify(executor).run(308L, CONFIG_ID, 1_000_000L, 100L);
    }

    @Test
    void getSimulation_notFoundForOperator_throws() {
        when(simRepo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getSimulation(99L, OPERATOR_ID))
                .isInstanceOf(SimulationNotFoundException.class);
    }

    // -------------------------------------------------------------------------

    private void stubOwnedConfig() {
        final GameConfigEntity config = mock(GameConfigEntity.class);
        when(config.getId()).thenReturn(CONFIG_ID);
        when(config.getGameId()).thenReturn(GAME_ID);
        when(config.getConfig()).thenReturn(CONFIG_1_LINE);
        when(configRepo.findById(CONFIG_ID)).thenReturn(Optional.of(config));

        final GameEntity game = mock(GameEntity.class);
        when(game.getOperatorId()).thenReturn(OPERATOR_ID);
        when(gameRepo.findById(GAME_ID)).thenReturn(Optional.of(game));
    }

    private static <T> T withId(final T entity, final long id) {
        try {
            final var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return entity;
    }
}
