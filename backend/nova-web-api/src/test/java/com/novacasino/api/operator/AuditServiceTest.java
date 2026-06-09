package com.novacasino.api.operator;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.operator.dto.RoundSummaryDto;
import com.novacasino.infrastructure.persistence.entity.GameRoundEntity;
import com.novacasino.infrastructure.persistence.repository.GameRoundJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for {@link AuditService}: default ordering and entity→DTO mapping. */
class AuditServiceTest {

    private static final long OPERATOR_ID = 1L;

    private GameRoundJpaRepository roundRepo;
    private AuditService service;

    @BeforeEach
    void setUp() {
        roundRepo = mock(GameRoundJpaRepository.class);
        service = new AuditService(roundRepo);
    }

    @SuppressWarnings("unchecked")
    @Test
    void appliesDefaultCreatedAtDescWhenUnsorted() {
        when(roundRepo.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.searchRounds(OPERATOR_ID, null, null, null, null, PageRequest.of(0, 20));

        final ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(roundRepo).findAll(any(Specification.class), captor.capture());
        final Sort.Order order = captor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @SuppressWarnings("unchecked")
    @Test
    void mapsRoundFieldsToSummary() {
        final GameRoundEntity round = withId(GameRoundEntity.baseRound(
                OPERATOR_ID, 7L, 3L, 11L, 999L, 100L, 500L, 100_000L, 100_400L, "{}"), 42L);
        final Page<GameRoundEntity> page = new PageImpl<>(List.of(round), PageRequest.of(0, 20), 1);
        when(roundRepo.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        final PageResponse<RoundSummaryDto> result =
                service.searchRounds(OPERATOR_ID, null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.totalElements()).isEqualTo(1);
        final RoundSummaryDto dto = result.content().get(0);
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.playerId()).isEqualTo(7L);
        assertThat(dto.gameId()).isEqualTo(3L);
        assertThat(dto.betCents()).isEqualTo(100L);
        assertThat(dto.winCents()).isEqualTo(500L);
        assertThat(dto.balancePostCents()).isEqualTo(100_400L);
        assertThat(dto.freeSpin()).isFalse();
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
