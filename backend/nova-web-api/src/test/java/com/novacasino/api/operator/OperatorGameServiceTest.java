package com.novacasino.api.operator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.operator.dto.OperatorGameDto;
import com.novacasino.api.operator.dto.UpdateGameRequest;
import com.novacasino.api.operator.exception.InvalidCommercialConfigException;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameCommercialAuditEntity;
import com.novacasino.infrastructure.persistence.entity.GameCommercialEntity;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.repository.GameCommercialAuditJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameCommercialJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for HU-15 commercial-config listing/update + audit in {@link OperatorGameService}. */
class OperatorGameServiceTest {

    private static final long OPERATOR_ID = 1L;
    private static final long GAME_ID     = 10L;
    private static final long USER_ID     = 7L;
    private static final long CONFIG_ID   = 100L;

    private GameCommercialJpaRepository gamesRepo;
    private GameCommercialAuditJpaRepository auditRepo;
    private GameConfigJpaRepository configRepo;
    private OperatorGameService service;

    @BeforeEach
    void setUp() {
        gamesRepo  = mock(GameCommercialJpaRepository.class);
        auditRepo  = mock(GameCommercialAuditJpaRepository.class);
        configRepo = mock(GameConfigJpaRepository.class);
        service = new OperatorGameService(gamesRepo, auditRepo, configRepo, new ObjectMapper());

        when(gamesRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // Active config with 5 paylines.
        when(configRepo.findById(CONFIG_ID)).thenReturn(Optional.of(configWithPaylines(5)));
    }

    // --- AC1: list maps commercial fields, currencies and payline count ---

    @Test
    void listGames_mapsCommercialConfig() {
        when(gamesRepo.findByOperatorIdOrderByIdAsc(OPERATOR_ID))
                .thenReturn(List.of(game(100, 10000, 100, new String[]{"EUR"}, true)));

        final List<OperatorGameDto> dtos = service.listGames(OPERATOR_ID);

        assertThat(dtos).hasSize(1);
        final OperatorGameDto dto = dtos.get(0);
        assertThat(dto.allowedCurrencies()).containsExactly("EUR");
        assertThat(dto.paylineCount()).isEqualTo(5);
        assertThat(dto.minBetCents()).isEqualTo(100);
    }

    // --- AC2: a valid update persists and writes an audit entry with before/after ---

    @Test
    void updateGame_valid_persistsAndAudits() {
        final GameCommercialEntity game = game(100, 10000, 100, new String[]{"EUR"}, true);
        when(gamesRepo.findByIdAndOperatorId(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(game));

        final UpdateGameRequest req = new UpdateGameRequest(500L, 20000L, 500L, false, List.of("EUR", "USD"));
        final OperatorGameDto dto = service.updateGame(OPERATOR_ID, GAME_ID, USER_ID, req);

        assertThat(dto.maxBetCents()).isEqualTo(20000);
        assertThat(dto.active()).isFalse();
        assertThat(dto.allowedCurrencies()).containsExactly("EUR", "USD");
        assertThat(game.getMinBetCents()).isEqualTo(500); // entity mutated

        final ArgumentCaptor<GameCommercialAuditEntity> captor =
                ArgumentCaptor.forClass(GameCommercialAuditEntity.class);
        verify(auditRepo).save(captor.capture());
        final GameCommercialAuditEntity audit = captor.getValue();
        assertThat(audit.getPerformedByUserId()).isEqualTo(USER_ID);
        assertThat(audit.getBeforeValue()).contains("\"maxBetCents\":10000");
        assertThat(audit.getAfterValue()).contains("\"maxBetCents\":20000").contains("\"active\":false");
    }

    // --- AC3: bet/step not a multiple of the payline count → 422, nothing persisted ---

    @Test
    void updateGame_betNotMultipleOfPaylines_throws() {
        final GameCommercialEntity game = game(100, 10000, 100, new String[]{"EUR"}, true);
        when(gamesRepo.findByIdAndOperatorId(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(game));

        // 501 is not a multiple of 5 paylines.
        final UpdateGameRequest req = new UpdateGameRequest(501L, 20000L, 500L, true, null);
        assertThatThrownBy(() -> service.updateGame(OPERATOR_ID, GAME_ID, USER_ID, req))
                .isInstanceOf(InvalidCommercialConfigException.class);
        verify(gamesRepo, never()).save(any());
        verify(auditRepo, never()).save(any());
    }

    @Test
    void updateGame_maxLessThanMin_throws() {
        final GameCommercialEntity game = game(100, 10000, 100, new String[]{"EUR"}, true);
        when(gamesRepo.findByIdAndOperatorId(GAME_ID, OPERATOR_ID)).thenReturn(Optional.of(game));

        final UpdateGameRequest req = new UpdateGameRequest(1000L, 500L, 100L, true, null);
        assertThatThrownBy(() -> service.updateGame(OPERATOR_ID, GAME_ID, USER_ID, req))
                .isInstanceOf(InvalidCommercialConfigException.class);
    }

    // --- AC5: a game of another operator is not found ---

    @Test
    void updateGame_gameOfAnotherOperator_throwsNotFound() {
        when(gamesRepo.findByIdAndOperatorId(GAME_ID, OPERATOR_ID)).thenReturn(Optional.empty());

        final UpdateGameRequest req = new UpdateGameRequest(500L, 20000L, 500L, true, null);
        assertThatThrownBy(() -> service.updateGame(OPERATOR_ID, GAME_ID, USER_ID, req))
                .isInstanceOf(GameNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static GameCommercialEntity game(final long min, final long max, final long step,
                                             final String[] currencies, final boolean active) {
        final GameCommercialEntity g = new GameCommercialEntity();
        setField(g, "id", GAME_ID);
        setField(g, "operatorId", OPERATOR_ID);
        setField(g, "code", "fruits-3x3");
        setField(g, "name", "Frutas");
        setField(g, "theme", "FRUITS");
        setField(g, "activeConfigId", CONFIG_ID);
        g.setMinBetCents(min);
        g.setMaxBetCents(max);
        g.setBetStepCents(step);
        g.setAllowedCurrencies(currencies);
        g.setActive(active);
        return g;
    }

    private static GameConfigEntity configWithPaylines(final int n) {
        final StringBuilder sb = new StringBuilder("{\"paylines\":[");
        for (int i = 0; i < n; i++) {
            sb.append(i == 0 ? "[0,1,2]" : ",[0,1,2]");
        }
        sb.append("]}");
        return new GameConfigEntity(GAME_ID, 1, sb.toString(),
                new BigDecimal("0.95"), null, USER_ID, null);
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
