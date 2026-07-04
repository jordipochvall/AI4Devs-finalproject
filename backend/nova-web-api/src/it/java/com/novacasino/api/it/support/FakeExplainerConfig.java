package com.novacasino.api.it.support;

import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Deterministic {@link Explainer} for the AI-explainability IT (HU-8-QA-01): it makes no real network
 * call to Anthropic. Only active when a test explicitly {@code @Import}s it. Marked {@code @Primary}
 * so it takes precedence over the default {@code OfflineExplainer} in that test context.
 */
@TestConfiguration
public class FakeExplainerConfig {

    public static final String MODEL = "fake-explainer-1";

    @Bean
    @Primary
    Explainer fakeExplainer() {
        return prompt -> new Explanation("Deterministic explanation (" + prompt.length() + " chars).", MODEL);
    }
}
