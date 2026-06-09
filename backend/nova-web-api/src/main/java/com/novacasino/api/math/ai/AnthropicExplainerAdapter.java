package com.novacasino.api.math.ai;

import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Production {@link Explainer} backed by Anthropic's Messages API (Claude). Activated only when
 * {@code anthropic.enabled=true} (i.e. an API key is configured); otherwise no bean exists and the
 * {@code explain} endpoint reports 503 while the rest of the platform keeps working (HU-8 AC4).
 *
 * <p>It is the only external runtime service (readme §2.2.4). Prompt caching is deferred (post-MVP).
 */
@Component
@ConditionalOnProperty(name = "anthropic.enabled", havingValue = "true")
public class AnthropicExplainerAdapter implements Explainer {

    private final RestClient client;
    private final String apiKey;
    private final String model;

    public AnthropicExplainerAdapter(
            @Value("${anthropic.base-url:https://api.anthropic.com}") final String baseUrl,
            @Value("${anthropic.api-key:}") final String apiKey,
            @Value("${anthropic.model:claude-haiku-4-5}") final String model,
            @Value("${anthropic.version:2023-06-01}") final String version) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("anthropic-version", version)
                .defaultHeader("content-type", "application/json")
                .build();
    }

    @Override
    public Explanation explain(final String prompt) {
        final Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 1024,
                "messages", List.of(Map.of("role", "user", "content", prompt)));

        final AnthropicResponse response = client.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .body(body)
                .retrieve()
                .body(AnthropicResponse.class);

        final String answer = (response != null && response.content() != null && !response.content().isEmpty())
                ? response.content().get(0).text()
                : "";
        return new Explanation(answer, model);
    }

    /** Minimal projection of the Messages API response. */
    private record AnthropicResponse(List<ContentBlock> content) {
        private record ContentBlock(String type, String text) {
        }
    }
}
