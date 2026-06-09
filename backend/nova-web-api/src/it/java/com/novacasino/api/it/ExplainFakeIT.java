package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.novacasino.api.it.support.FakeExplainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-8-QA-01 — explainability with a deterministic fake adapter (no real Anthropic call, AC2): a
 * completed simulation's explain returns 200 and the Q&A is persisted with the right model (AC1/AC3).
 * The 422/404/403/503 codes are covered by ExplainServiceTest + ExplainIT.
 */
@Import(FakeExplainerConfig.class)
class ExplainFakeIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private long fruitsConfigId(final String token) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/math/games")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        for (final JsonNode game : objectMapper.readTree(json)) {
            if ("FRUITS".equals(game.get("theme").asText())) { return game.get("activeConfigId").asLong(); }
        }
        throw new IllegalStateException("Fruits config not found");
    }

    private long launchAndComplete(final String token, final long configId) throws Exception {
        final String accepted = mockMvc.perform(post("/api/v1/math/configs/" + configId + "/simulations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"numSpins": 5000, "betCents": 100}"""))
                .andExpect(status().isAccepted()).andReturn().getResponse().getContentAsString();
        final long id = objectMapper.readTree(accepted).get("simulationId").asLong();
        for (int i = 0; i < 100; i++) {
            final String status = mockMvc.perform(get("/api/v1/math/simulations/" + id)
                            .header("Authorization", "Bearer " + token))
                    .andReturn().getResponse().getContentAsString();
            if (!"RUNNING".equals(objectMapper.readTree(status).get("status").asText())) { break; }
            Thread.sleep(100);
        }
        return id;
    }

    @Test
    void explain_completed_returns200AndPersistsWithModel() throws Exception {
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        final long simId = launchAndComplete(token, fruitsConfigId(token));

        mockMvc.perform(post("/api/v1/math/simulations/" + simId + "/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"question": "How healthy is the RTP?"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").exists())
                .andExpect(jsonPath("$.model").value(FakeExplainerConfig.MODEL))
                .andExpect(jsonPath("$.askedAt").exists());

        // The Q&A is persisted with the right model (AC3).
        final String model = jdbc.queryForObject(
                "SELECT model FROM simulation_explanations WHERE simulation_run_id = ? ORDER BY id DESC LIMIT 1",
                String.class, simId);
        assertThat(model).isEqualTo(FakeExplainerConfig.MODEL);
    }
}
