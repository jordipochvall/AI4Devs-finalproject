package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-8-BE-01 — the AI explain endpoint over a real DB, with the AI <strong>disabled</strong> (no
 * ANTHROPIC_API_KEY in the test env): a completed simulation's explain returns 503 while the rest of
 * the platform works (AC4); unknown simulation → 404 (AC3); non-MATH role → 403 (AC5).
 */
class ExplainIT extends AbstractIntegrationTest {

    private long fruitsConfigId(final String mathToken) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/math/games")
                        .header("Authorization", "Bearer " + mathToken))
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

    // --- AC4: AI disabled → completed simulation explain returns 503 ---

    @Test
    void explain_aiDisabled_serviceUnavailable() throws Exception {
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        final long simId = launchAndComplete(token, fruitsConfigId(token));

        mockMvc.perform(post("/api/v1/math/simulations/" + simId + "/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"question": "Is the RTP healthy?"}"""))
                .andExpect(status().isServiceUnavailable());
    }

    // --- AC3: unknown simulation → 404 ---

    @Test
    void explain_unknownSimulation_notFound() throws Exception {
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        mockMvc.perform(post("/api/v1/math/simulations/99999999/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"question": "anything"}"""))
                .andExpect(status().isNotFound());
    }

    // --- AC5: non-MATH role → 403 ---

    @Test
    void explain_withPlayerToken_forbidden() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(post("/api/v1/math/simulations/1/explain")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"question": "anything"}"""))
                .andExpect(status().isForbidden());
    }
}
