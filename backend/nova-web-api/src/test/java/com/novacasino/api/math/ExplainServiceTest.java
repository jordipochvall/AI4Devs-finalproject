package com.novacasino.api.math;

import com.novacasino.api.math.dto.ExplanationDto;
import com.novacasino.api.math.exception.ExplainerUnavailableException;
import com.novacasino.api.math.exception.SimulationNotCompletedException;
import com.novacasino.api.math.exception.SimulationNotFoundException;
import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import com.novacasino.infrastructure.persistence.entity.SimulationExplanationEntity;
import com.novacasino.infrastructure.persistence.entity.SimulationRunEntity;
import com.novacasino.infrastructure.persistence.repository.SimulationExplanationJpaRepository;
import com.novacasino.infrastructure.persistence.repository.SimulationRunJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Unit tests for {@link ExplainService}: 200 path, not-completed (422), not-found (404), no-AI (503). */
class ExplainServiceTest {

    private static final long OPERATOR_ID = 1L;
    private static final long USER_ID = 9L;
    private static final long SIM_ID = 308L;

    private SimulationRunJpaRepository simRepo;
    private SimulationExplanationJpaRepository explanationRepo;
    private Explainer explainer;

    @BeforeEach
    void setUp() {
        simRepo = mock(SimulationRunJpaRepository.class);
        explanationRepo = mock(SimulationExplanationJpaRepository.class);
        explainer = mock(Explainer.class);
    }

    private ExplainService service(final Optional<Explainer> ai) {
        return new ExplainService(simRepo, explanationRepo, ai);
    }

    private SimulationRunEntity run(final String status) {
        final SimulationRunEntity r = mock(SimulationRunEntity.class);
        when(r.getOperatorId()).thenReturn(OPERATOR_ID);
        when(r.getStatus()).thenReturn(status);
        return r;
    }

    @Test
    void explain_completed_returnsAnswerAndPersists() {
        final SimulationRunEntity completed = run(SimulationRunEntity.COMPLETED);
        when(simRepo.findById(SIM_ID)).thenReturn(Optional.of(completed));
        when(explainer.explain(anyString())).thenReturn(new Explanation("RTP looks healthy.", "claude-haiku-4-5"));
        when(explanationRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final ExplanationDto dto = service(Optional.of(explainer))
                .explain(SIM_ID, OPERATOR_ID, USER_ID, "Is the RTP ok?");

        assertThat(dto.answer()).isEqualTo("RTP looks healthy.");
        assertThat(dto.model()).isEqualTo("claude-haiku-4-5");
        verify(explanationRepo).save(any(SimulationExplanationEntity.class));
    }

    @Test
    void explain_notCompleted_throws422() {
        final SimulationRunEntity running = run(SimulationRunEntity.RUNNING);
        when(simRepo.findById(SIM_ID)).thenReturn(Optional.of(running));
        assertThatThrownBy(() -> service(Optional.of(explainer)).explain(SIM_ID, OPERATOR_ID, USER_ID, "q"))
                .isInstanceOf(SimulationNotCompletedException.class);
        verifyNoInteractions(explainer);
    }

    @Test
    void explain_notFound_throws404() {
        when(simRepo.findById(SIM_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service(Optional.of(explainer)).explain(SIM_ID, OPERATOR_ID, USER_ID, "q"))
                .isInstanceOf(SimulationNotFoundException.class);
    }

    @Test
    void explain_noExplainer_throws503() {
        final SimulationRunEntity completed = run(SimulationRunEntity.COMPLETED);
        when(simRepo.findById(SIM_ID)).thenReturn(Optional.of(completed));
        assertThatThrownBy(() -> service(Optional.empty()).explain(SIM_ID, OPERATOR_ID, USER_ID, "q"))
                .isInstanceOf(ExplainerUnavailableException.class);
    }
}
