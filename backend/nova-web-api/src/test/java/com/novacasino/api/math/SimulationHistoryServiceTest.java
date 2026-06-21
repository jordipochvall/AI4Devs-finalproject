package com.novacasino.api.math;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.math.dto.ExplanationDto;
import com.novacasino.api.math.dto.SimulationSummaryDto;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.infrastructure.persistence.entity.SimulationExplanationEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.SimulationExplanationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-18 simulation/AI history in {@link SimulationHistoryService}. */
class SimulationHistoryServiceTest {

    private static final long OPERATOR_ID = 1L;

    private SimulationRunJpaRepository simRepo;
    private SimulationExplanationJpaRepository explanationRepo;
    private SimulationHistoryService service;

    private final Pageable pageable = PageRequest.of(0, 20);

    @BeforeEach
    void setUp() {
        simRepo = mock(SimulationRunJpaRepository.class);
        explanationRepo = mock(SimulationExplanationJpaRepository.class);
        service = new SimulationHistoryService(simRepo, explanationRepo);
    }

    @Test
    void listSimulations_mapsSummaries() {
        final SimulationRunEntity run = run(OPERATOR_ID, 100L);
        setId(run, 50L);
        when(simRepo.search(eq(OPERATOR_ID), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(run), pageable, 1));

        final PageResponse<SimulationSummaryDto> page = service.listSimulations(OPERATOR_ID, null, null, pageable);

        assertThat(page.content()).singleElement().satisfies(dto -> {
            assertThat(dto.id()).isEqualTo(50L);
            assertThat(dto.gameConfigId()).isEqualTo(100L);
            assertThat(dto.status()).isEqualTo("RUNNING");
        });
    }

    @Test
    void listExplanations_returnsThreadForOwnedSimulation() {
        final SimulationRunEntity run = run(OPERATOR_ID, 100L);
        setId(run, 50L);
        when(simRepo.findById(50L)).thenReturn(Optional.of(run));
        when(explanationRepo.findBySimulationRunIdOrderByAskedAtAsc(50L))
                .thenReturn(List.of(new SimulationExplanationEntity(50L, 9L, "¿RTP ok?", "Sí.", "claude-haiku-4-5")));

        final List<ExplanationDto> thread = service.listExplanations(50L, OPERATOR_ID);

        assertThat(thread).singleElement().satisfies(e -> {
            assertThat(e.question()).isEqualTo("¿RTP ok?");
            assertThat(e.model()).isEqualTo("claude-haiku-4-5");
        });
    }

    @Test
    void listExplanations_foreignSimulation_throwsNotFound() {
        final SimulationRunEntity run = run(2L, 100L); // another operator
        setId(run, 50L);
        when(simRepo.findById(50L)).thenReturn(Optional.of(run));

        assertThatThrownBy(() -> service.listExplanations(50L, OPERATOR_ID))
                .isInstanceOf(SimulationNotFoundException.class);
        verifyNoInteractions(explanationRepo);
    }

    // -------------------------------------------------------------------------

    private static SimulationRunEntity run(final long operatorId, final long configId) {
        return new SimulationRunEntity(operatorId, configId, 9L, 1000L, 100L);
    }

    private static void setId(final Object entity, final Long id) {
        try {
            final var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
