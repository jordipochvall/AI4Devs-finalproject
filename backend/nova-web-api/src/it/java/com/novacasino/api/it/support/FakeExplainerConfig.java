package com.novacasino.api.it.support;

import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Deterministic {@link Explainer} for the AI-explainability IT (HU-8-QA-01): it makes no real network
 * call to Anthropic. Only active when a test explicitly {@code @Import}s it, so it does not leak into
 * the other integration contexts (where the explainer stays absent and {@code explain} returns 503).
 */
@TestConfiguration
public class FakeExplainerConfig {

    public static final String MODEL = "fake-explainer-1";

    @Bean
    Explainer fakeExplainer() {
        return prompt -> new Explanation("Deterministic explanation (" + prompt.length() + " chars).", MODEL);
    }
}
