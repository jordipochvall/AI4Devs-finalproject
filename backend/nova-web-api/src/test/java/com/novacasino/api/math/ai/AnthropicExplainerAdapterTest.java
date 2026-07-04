package com.novacasino.api.math.ai;

import com.novacasino.application.math.exception.ExplainerUnavailableException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HU-32 AC3 — a provider failure (unreachable endpoint / bad key) degrades to 503, not 500. */
class AnthropicExplainerAdapterTest {

    @Test
    void providerFailure_degradesToUnavailable() {
        // Unreachable base URL → the RestClient call throws → the adapter must translate it into
        // ExplainerUnavailableException (which the GlobalExceptionHandler maps to HTTP 503).
        final AnthropicExplainerAdapter adapter =
                new AnthropicExplainerAdapter("http://127.0.0.1:1", "test-key", "test-model", "2023-06-01");

        assertThatThrownBy(() -> adapter.explain("metrics + question"))
                .isInstanceOf(ExplainerUnavailableException.class);
    }
}
