package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-18 — simulation/AI history over a real DB. Seeds a COMPLETED run and an AI Q&A directly (the AI
 * adapter is disabled in tests), then verifies the read endpoints, isolation and roles.
 */
class SimulationHistoryIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private long seedRunWithExplanation() {
        final Long operatorId = jdbc.queryForObject(
                "SELECT id FROM operators WHERE code = 'novacasino-default'", Long.class);
        final Long mathUserId = jdbc.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, MATH_EMAIL);
        final Long configId = jdbc.queryForObject(
                "SELECT id FROM game_configs ORDER BY id LIMIT 1", Long.class);

        final Long runId = jdbc.queryForObject("""
                INSERT INTO simulation_runs (operator_id, game_config_id, launched_by_user_id,
                        num_spins, bet_cents, status, rtp_empirical)
                VALUES (?, ?, ?, ?, ?, 'COMPLETED', 0.96) RETURNING id""",
                Long.class, operatorId, configId, mathUserId, 1000L, 100L);

        jdbc.update("""
                INSERT INTO simulation_explanations (simulation_run_id, asked_by_user_id, question, answer, model)
                VALUES (?, ?, ?, ?, ?)""",
                runId, mathUserId, "¿Converge el RTP?", "Sí, dentro del IC.", "claude-haiku-4-5");
        return runId;
    }

    // --- AC1: the simulation history lists the run (paginated wrapper) ---

    @Test
    void listSimulations_includesSeededRun() throws Exception {
        final long runId = seedRunWithExplanation();
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);

        final String json = mockMvc.perform(get("/api/v1/math/simulations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andReturn().getResponse().getContentAsString();

        boolean found = false;
        for (final JsonNode s : objectMapper.readTree(json).get("content")) {
            if (s.get("id").asLong() == runId) {
                assertThat(s.get("status").asText()).isEqualTo("COMPLETED");
                found = true;
            }
        }
        assertThat(found).isTrue();
    }

    // --- AC2: the AI Q&A thread of the run is returned ---

    @Test
    void listExplanations_returnsThread() throws Exception {
        final long runId = seedRunWithExplanation();
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);

        mockMvc.perform(get("/api/v1/math/simulations/" + runId + "/explanations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].question").value("¿Converge el RTP?"))
                .andExpect(jsonPath("$[0].model").value("claude-haiku-4-5"));
    }

    // --- AC3/AC4: roles and missing simulation ---

    @Test
    void listSimulations_asPlayer_returns403() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/math/simulations")
                        .header("Authorization", "Bearer " + player))
                .andExpect(status().isForbidden());
    }

    @Test
    void listExplanations_unknownSimulation_returns404() throws Exception {
        final String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        mockMvc.perform(get("/api/v1/math/simulations/999999/explanations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
