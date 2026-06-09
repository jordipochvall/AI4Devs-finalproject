package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-2-BE-02 — the asynchronous simulation API over a real DB. Launches a run (202 RUNNING), polls
 * until COMPLETED and checks the metrics (AC1/AC3/AC4), validates numSpins range (AC2) and role
 * enforcement (AC6). Uses the "Frutas" config (no free spins → fast, bounded).
 */
class SimulationIT extends AbstractIntegrationTest {

    private long fruitsConfigId(final String mathToken) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/math/games")
                        .header("Authorization", "Bearer " + mathToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode game : objectMapper.readTree(json)) {
            if ("FRUITS".equals(game.get("theme").asText())) {
                return game.get("activeConfigId").asLong();
            }
        }
        throw new IllegalStateException("Fruits seed config not found");
    }

    // --- AC1/AC3/AC4: launch → RUNNING → poll → COMPLETED with metrics ---

    @Test
    void launchPollCompletesWithMetrics() throws Exception {
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        final long configId = fruitsConfigId(token);

        final String accepted = mockMvc.perform(post("/api/v1/math/configs/" + configId + "/simulations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"numSpins": 20000, "betCents": 100}"""))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.simulationId").isNumber())
                .andExpect(jsonPath("$.status").value("RUNNING"))
                .andExpect(jsonPath("$.pollUrl").exists())
                .andReturn().getResponse().getContentAsString();
        final long simulationId = objectMapper.readTree(accepted).get("simulationId").asLong();

        final JsonNode result = pollUntilCompleted(token, simulationId);
        assertThat(result.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(result.get("rtpEmpirical").isNull()).isFalse();
        assertThat(result.get("durationMs").isNull()).isFalse();
        assertThat(result.get("numSpins").asLong()).isEqualTo(20000L);
        // The detailed metrics are populated too (rtp_std_error, convergence_sample, rtp_breakdown).
        assertThat(result.get("rtpStdError").isNull()).isFalse();
        assertThat(result.get("prizeDistribution").isObject()).isTrue();
        assertThat(result.get("convergenceSample").isArray()).isTrue();
        assertThat(result.get("rtpBreakdown").isObject()).isTrue();
    }

    // --- AC2: numSpins out of range → 422 ---

    @Test
    void launchOutOfRange_unprocessable() throws Exception {
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        final long configId = fruitsConfigId(token);

        mockMvc.perform(post("/api/v1/math/configs/" + configId + "/simulations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"numSpins": 20000000, "betCents": 100}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    // --- AC6: a non-MATH role cannot launch simulations → 403 ---

    @Test
    void launchWithPlayerToken_forbidden() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(post("/api/v1/math/configs/1/simulations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"numSpins": 1000, "betCents": 100}"""))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------

    /** Polls the status endpoint until COMPLETED/FAILED or a timeout. */
    private JsonNode pollUntilCompleted(final String token, final long simulationId) throws Exception {
        for (int attempt = 0; attempt < 100; attempt++) {
            final String json = mockMvc.perform(get("/api/v1/math/simulations/" + simulationId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            final JsonNode node = objectMapper.readTree(json);
            final String status = node.get("status").asText();
            if (!"RUNNING".equals(status)) {
                return node;
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Simulation " + simulationId + " did not finish in time");
    }
}
