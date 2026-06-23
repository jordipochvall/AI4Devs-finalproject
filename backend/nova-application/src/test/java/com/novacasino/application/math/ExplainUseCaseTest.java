package com.novacasino.application.math;

import com.novacasino.application.math.SimulationQueryPort.SimulationView;
import com.novacasino.application.math.exception.ExplainerUnavailableException;
import com.novacasino.application.math.exception.SimulationNotCompletedException;
import com.novacasino.application.math.exception.SimulationNotFoundException;
import com.novacasino.common.dto.ExplanationDto;
import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-8 AI explanation in {@link ExplainUseCase} (port + explainer mocked). */
class ExplainUseCaseTest {

    private static final long SIM = 5L;
    private static final long OPERATOR = 1L;
    private static final long USER = 9L;

    private SimulationQueryPort port;

    @BeforeEach
    void setUp() {
        port = mock(SimulationQueryPort.class);
    }

    private static SimulationView view(final String status) {
        return new SimulationView(status, 1_000_000L, 100L, 0.95, 0.001, 0.6, 0.35,
                0.25, 12.3, 500.0, 0.04, 80);
    }

    private ExplainUseCase useCase(final Optional<Explainer> explainer) {
        return new ExplainUseCase(port, explainer);
    }

    @Test
    void explain_simulationMissing_throwsNotFound() {
        when(port.findForExplain(SIM, OPERATOR)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase(Optional.empty()).explain(SIM, OPERATOR, USER, "q"))
                .isInstanceOf(SimulationNotFoundException.class);
    }

    @Test
    void explain_notCompleted_throws() {
        when(port.findForExplain(SIM, OPERATOR)).thenReturn(Optional.of(view("RUNNING")));
        final Explainer ai = mock(Explainer.class);
        assertThatThrownBy(() -> useCase(Optional.of(ai)).explain(SIM, OPERATOR, USER, "q"))
                .isInstanceOf(SimulationNotCompletedException.class);
        verifyNoInteractions(ai);
    }

    @Test
    void explain_noExplainer_throwsUnavailable() {
        when(port.findForExplain(SIM, OPERATOR)).thenReturn(Optional.of(view("COMPLETED")));
        assertThatThrownBy(() -> useCase(Optional.empty()).explain(SIM, OPERATOR, USER, "q"))
                .isInstanceOf(ExplainerUnavailableException.class);
    }

    @Test
    void explain_completed_asksAiAndPersists() {
        when(port.findForExplain(SIM, OPERATOR)).thenReturn(Optional.of(view("COMPLETED")));
        final Explainer ai = mock(Explainer.class);
        when(ai.explain(any())).thenReturn(new Explanation("the answer", "claude-haiku-4-5"));
        final ExplanationDto dto = new ExplanationDto("q", "the answer", "claude-haiku-4-5", OffsetDateTime.now());
        when(port.saveExplanation(eq(SIM), eq(USER), eq("q"), eq("the answer"), eq("claude-haiku-4-5")))
                .thenReturn(dto);

        assertThat(useCase(Optional.of(ai)).explain(SIM, OPERATOR, USER, "q").answer()).isEqualTo("the answer");
        verify(ai).explain(any());
    }
}
