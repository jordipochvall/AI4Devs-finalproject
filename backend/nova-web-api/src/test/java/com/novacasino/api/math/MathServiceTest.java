package com.novacasino.api.math;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.math.dto.ConfigVersionDto;
import com.novacasino.api.math.dto.PublishResultDto;
import com.novacasino.api.math.exception.ConfigAlreadyActiveException;
import com.novacasino.api.math.exception.ConfigNotFoundException;
import com.novacasino.api.math.validation.ConfigValidator;
import com.novacasino.api.player.exception.GameNotFoundException;
import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import com.novacasino.infrastructure.persistence.entity.GameConfigPublicationEntity;
import com.novacasino.infrastructure.persistence.entity.GameEntity;
import com.novacasino.infrastructure.persistence.repository.GameConfigJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameConfigPublicationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.GameJpaRepository;
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

/** Unit tests for HU-17 publish/version-history logic in {@link MathService} with mocked repos. */
class MathServiceTest {

    private static final long OPERATOR_ID = 1L;
    private static final long GAME_ID     = 10L;
    private static final long MATH_USER   = 99L;

    private GameJpaRepository gameRepo;
    private GameConfigJpaRepository configRepo;
    private GameConfigPublicationJpaRepository publicationRepo;
    private MathService service;

    @BeforeEach
    void setUp() {
        gameRepo        = mock(GameJpaRepository.class);
        configRepo      = mock(GameConfigJpaRepository.class);
        publicationRepo = mock(GameConfigPublicationJpaRepository.class);
        service = new MathService(gameRepo, configRepo, publicationRepo,
                new ConfigValidator(), new ObjectMapper());

        when(gameRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(publicationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // --- AC1: list versions flags the active one ---

    @Test
    void listConfigs_flagsActiveVersion() {
        final GameEntity game = game(GAME_ID, OPERATOR_ID, 2L); // version with id=2 is active
        when(gameRepo.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(configRepo.findByGameIdOrderByVersionDesc(GAME_ID))
                .thenReturn(List.of(config(2L, 2), config(1L, 1)));

        final List<ConfigVersionDto> versions = service.listConfigs(GAME_ID, OPERATOR_ID);

        assertThat(versions).extracting(ConfigVersionDto::version).containsExactly(2, 1);
        assertThat(versions).filteredOn(ConfigVersionDto::active)
                .extracting(ConfigVersionDto::id).containsExactly(2L);
    }

    @Test
    void listConfigs_gameOfAnotherOperator_throwsNotFound() {
        when(gameRepo.findById(GAME_ID)).thenReturn(Optional.of(game(GAME_ID, 2L, null)));
        assertThatThrownBy(() -> service.listConfigs(GAME_ID, OPERATOR_ID))
                .isInstanceOf(GameNotFoundException.class);
    }

    // --- AC2: publishing moves the active pointer and records the publication ---

    @Test
    void publish_movesActivePointerAndRecordsPublication() {
        final GameEntity game = game(GAME_ID, OPERATOR_ID, 1L); // currently active = id 1
        when(gameRepo.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(configRepo.findById(2L)).thenReturn(Optional.of(config(2L, 2)));

        final PublishResultDto result = service.publish(GAME_ID, 2L, OPERATOR_ID, MATH_USER);

        assertThat(result.activeConfigId()).isEqualTo(2L);
        assertThat(result.version()).isEqualTo(2);
        assertThat(result.publishedByUserId()).isEqualTo(MATH_USER);
        assertThat(game.getActiveConfigId()).isEqualTo(2L); // pointer moved

        final ArgumentCaptor<GameConfigPublicationEntity> captor =
                ArgumentCaptor.forClass(GameConfigPublicationEntity.class);
        verify(publicationRepo).save(captor.capture());
        assertThat(captor.getValue().getGameConfigId()).isEqualTo(2L);
        assertThat(captor.getValue().getPublishedByUserId()).isEqualTo(MATH_USER);
    }

    // --- AC3: publishing the already-active version → 409 ---

    @Test
    void publish_alreadyActive_throwsConflict() {
        final GameEntity game = game(GAME_ID, OPERATOR_ID, 2L); // id 2 already active
        when(gameRepo.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(configRepo.findById(2L)).thenReturn(Optional.of(config(2L, 2)));

        assertThatThrownBy(() -> service.publish(GAME_ID, 2L, OPERATOR_ID, MATH_USER))
                .isInstanceOf(ConfigAlreadyActiveException.class);
        verify(publicationRepo, never()).save(any());
    }

    @Test
    void publish_configOfAnotherGame_throwsNotFound() {
        when(gameRepo.findById(GAME_ID)).thenReturn(Optional.of(game(GAME_ID, OPERATOR_ID, 1L)));
        final GameConfigEntity foreign = config(5L, 1);
        setField(foreign, "gameId", 999L); // belongs to a different game
        when(configRepo.findById(5L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.publish(GAME_ID, 5L, OPERATOR_ID, MATH_USER))
                .isInstanceOf(ConfigNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static GameEntity game(final long id, final long operatorId, final Long activeConfigId) {
        final GameEntity g = new GameEntity();
        setField(g, "id", id);
        setField(g, "operatorId", operatorId);
        g.setActiveConfigId(activeConfigId);
        return g;
    }

    private static GameConfigEntity config(final long id, final int version) {
        final GameConfigEntity c = new GameConfigEntity(GAME_ID, version, "{}",
                new BigDecimal("0.95"), new BigDecimal("0.5"), MATH_USER, null);
        setField(c, "id", id);
        return c;
    }

    /** Sets a private field by reflection (entities expose no id/operatorId setter). */
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
