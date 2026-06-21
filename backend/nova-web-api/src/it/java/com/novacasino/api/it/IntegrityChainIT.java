package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-20 — tamper-evident integrity chain over a real DB. Spins to build chain, verifies it intact,
 * then tampers a row by direct SQL (bypassing the immutability trigger) and checks detection. The
 * tamper is reverted so the shared cached context stays consistent for other ITs.
 */
class IntegrityChainIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private long spin(final String token, final long gameId) throws Exception {
        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("roundId").asLong();
    }

    private long fruitsGameId(final String token) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode g : objectMapper.readTree(json)) {
            if ("FRUITS".equals(g.get("theme").asText())) {
                return g.get("id").asLong();
            }
        }
        throw new IllegalStateException("Fruits seed game not found");
    }

    // --- AC1: a normally-built chain verifies intact ---

    @Test
    void verify_intactChain_isConsistent() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        spin(player, fruitsGameId(player));
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/operator/audit/integrity")
                        .header("Authorization", "Bearer " + operator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consistent").value(true))
                .andExpect(jsonPath("$.firstBrokenRoundId").doesNotExist());
    }

    // --- AC2: tampering a row by SQL is detected and pinpointed ---

    @Test
    void verify_tamperedRow_isDetected() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long roundId = spin(player, fruitsGameId(player));
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        final long originalWin = jdbc.queryForObject(
                "SELECT win_cents FROM game_rounds WHERE id = ?", Long.class, roundId);

        // Tamper: bypass the immutability trigger and alter the row's amount.
        jdbc.execute("ALTER TABLE game_rounds DISABLE TRIGGER trg_game_rounds_no_update_delete");
        try {
            jdbc.update("UPDATE game_rounds SET win_cents = win_cents + 1 WHERE id = ?", roundId);

            mockMvc.perform(get("/api/v1/operator/audit/integrity")
                            .header("Authorization", "Bearer " + operator))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.consistent").value(false))
                    .andExpect(jsonPath("$.firstBrokenRoundId").value(roundId));

            // Revert so the chain is consistent again for the rest of the suite.
            jdbc.update("UPDATE game_rounds SET win_cents = ? WHERE id = ?", originalWin, roundId);
        } finally {
            jdbc.execute("ALTER TABLE game_rounds ENABLE TRIGGER trg_game_rounds_no_update_delete");
        }

        // After reverting, the chain verifies intact again.
        mockMvc.perform(get("/api/v1/operator/audit/integrity")
                        .header("Authorization", "Bearer " + operator))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.consistent").value(true));
    }

    // --- AC3: a non-operator role cannot verify ---

    @Test
    void verify_asPlayer_returns403() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/operator/audit/integrity")
                        .header("Authorization", "Bearer " + player))
                .andExpect(status().isForbidden());
    }
}
