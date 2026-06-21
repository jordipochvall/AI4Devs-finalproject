package com.novacasino.api.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.operator.dto.DashboardDto;
import com.novacasino.api.operator.dto.RoundDetailDto;
import com.novacasino.api.operator.exception.RoundNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.projection.TopGameProjection;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-16 dashboard aggregation + round detail in {@link OperatorDashboardService}. */
class OperatorDashboardServiceTest {

    private static final long OPERATOR_ID = 1L;

    private GameRoundJpaRepository roundRepo;
    private GameJpaRepository gameRepo;
    private OperatorDashboardService service;

    @BeforeEach
    void setUp() {
        roundRepo = mock(GameRoundJpaRepository.class);
        gameRepo  = mock(GameJpaRepository.class);
        service = new OperatorDashboardService(roundRepo, gameRepo, new ObjectMapper());
    }

    // --- AC1: aggregated KPIs, with top games resolved to their names ---

    @Test
    void dashboard_aggregatesKpisAndResolvesTopGames() {
        when(roundRepo.countActivePlayers(eq(OPERATOR_ID), any(), any())).thenReturn(7L);
        when(roundRepo.ggrCents(eq(OPERATOR_ID), any(), any())).thenReturn(12345L);
        when(roundRepo.countRounds(eq(OPERATOR_ID), any(), any())).thenReturn(40L);
        when(roundRepo.topGames(eq(OPERATOR_ID), any(), any(), any()))
                .thenReturn(List.of(topRow(3L, 25L), topRow(5L, 15L)));
        when(gameRepo.findAllById(List.of(3L, 5L)))
                .thenReturn(List.of(game(3L, "Frutas", "FRUITS"), game(5L, "Espacial", "SPACE")));

        final DashboardDto dto = service.dashboard(OPERATOR_ID, null, null);

        assertThat(dto.activePlayers()).isEqualTo(7L);
        assertThat(dto.ggrCents()).isEqualTo(12345L);
        assertThat(dto.totalRounds()).isEqualTo(40L);
        assertThat(dto.topGames()).hasSize(2);
        assertThat(dto.topGames().get(0).gameId()).isEqualTo(3L);
        assertThat(dto.topGames().get(0).name()).isEqualTo("Frutas");
        assertThat(dto.topGames().get(0).rounds()).isEqualTo(25L);
        // Null bounds default to a non-null window (so JPQL never binds null timestamps).
        assertThat(dto.from()).isNotNull();
        assertThat(dto.to()).isNotNull();
    }

    // --- AC2: round detail exposes amounts, the symbol grid and winning paylines ---

    @Test
    void roundDetail_returnsAmountsAndResult() {
        final String result = "{\"view\":[[\"A\",\"B\"],[\"C\",\"A\"]],\"winningPaylines\":[],\"scatterCount\":2}";
        final GameRoundEntity round = GameRoundEntity.baseRound(
                OPERATOR_ID, 9L, 3L, 100L, 42L, 100L, 250L, 1000L, 1150L, result);
        setId(round, 77L);
        when(roundRepo.findById(77L)).thenReturn(Optional.of(round));

        final RoundDetailDto dto = service.roundDetail(77L, OPERATOR_ID);

        assertThat(dto.roundId()).isEqualTo(77L);
        assertThat(dto.betCents()).isEqualTo(100L);
        assertThat(dto.winCents()).isEqualTo(250L);
        assertThat(dto.view()).hasSize(2);
        assertThat(dto.scatterCount()).isEqualTo(2);
    }

    // --- AC4: a round of another operator (or missing) → 404 ---

    @Test
    void roundDetail_otherOperator_throwsNotFound() {
        final GameRoundEntity round = GameRoundEntity.baseRound(
                99L, 9L, 3L, 100L, 42L, 100L, 0L, 1000L, 900L, "{\"view\":[],\"scatterCount\":0}");
        setId(round, 5L);
        when(roundRepo.findById(5L)).thenReturn(Optional.of(round));

        assertThatThrownBy(() -> service.roundDetail(5L, OPERATOR_ID))
                .isInstanceOf(RoundNotFoundException.class);
    }

    // -------------------------------------------------------------------------

    private static TopGameProjection topRow(final long gameId, final long rounds) {
        return new TopGameProjection() {
            public Long getGameId() { return gameId; }
            public long getRounds() { return rounds; }
        };
    }

    private static GameEntity game(final long id, final String name, final String theme) {
        final GameEntity g = new GameEntity();
        setField(g, "id", id);
        setField(g, "name", name);
        setField(g, "theme", theme);
        return g;
    }

    private static void setId(final Object entity, final Long id) {
        setField(entity, "id", id);
    }

    private static void setField(final Object entity, final String field, final Object value) {
        try {
            final var f = entity.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(entity, value);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
