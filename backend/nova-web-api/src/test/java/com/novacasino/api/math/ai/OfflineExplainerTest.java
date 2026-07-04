package com.novacasino.api.math.ai;

import com.novacasino.domain.ai.Explanation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** HU-32 — the offline explainer builds a deterministic heuristic summary from the prompt metrics. */
class OfflineExplainerTest {

    @Test
    void summarizesMetricsFromPrompt() {
        final String prompt = """
                Simulation metrics:
                - spins: 1000000, betCents: 100
                - RTP empirical: 0.9403 (std error: 0.0012)
                - RTP base game: 0.5785, RTP free spins: 0.3618
                - hit frequency: 0.4618, volatility: 5.42
                - max win multiplier: 256.20, free-spins trigger freq: 0.0123
                - longest dry streak: 42
                Question: ¿El RTP es sano?
                """;

        final Explanation e = new OfflineExplainer().explain(prompt);

        assertThat(e.model()).isEqualTo(OfflineExplainer.MODEL);
        assertThat(e.answer())
                .contains("Análisis local")
                .contains("RTP empírico")
                .contains("94,0%")     // 0.9403 formatted es
                .contains("media");    // volatility 5.42 → "media"
    }
}
