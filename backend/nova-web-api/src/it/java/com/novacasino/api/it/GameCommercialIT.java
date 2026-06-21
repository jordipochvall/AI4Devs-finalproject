package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-15 — commercial-config endpoints over a real DB. To avoid breaking other ITs that share the
 * cached context (they spin 100-cent bets), updates keep min/step at 100 and only change maxBet and
 * currencies; the game stays active. Covers persistence (AC1), audit entry (AC2), the payline-multiple
 * validation (AC3) and role/isolation (AC4).
 */
class GameCommercialIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private JsonNode fruitsGame(final String operatorToken) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/operator/games")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode g : objectMapper.readTree(json)) {
            if ("FRUITS".equals(g.get("theme").asText())) {
                return g;
            }
        }
        throw new IllegalStateException("Fruits seed game not found");
    }

    // --- AC1 + AC2: a valid update persists, returns the new config and writes an audit entry ---

    @Test
    void update_persistsChange_andWritesAudit() throws Exception {
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        final JsonNode game = fruitsGame(token);
        final long gameId = game.get("id").asLong();
        assertThat(game.get("paylineCount").asInt()).isEqualTo(5);

        final String body = """
                {"minBetCents":100,"maxBetCents":20000,"betStepCents":100,"active":true,
                 "allowedCurrencies":["EUR","USD"]}""";

        // AC1: PUT returns the updated commercial config.
        mockMvc.perform(put("/api/v1/operator/games/" + gameId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxBetCents").value(20000))
                .andExpect(jsonPath("$.allowedCurrencies", org.hamcrest.Matchers.containsInAnyOrder("EUR", "USD")));

        // AC1: the change is persisted (a fresh GET reflects it).
        final String afterList = mockMvc.perform(get("/api/v1/operator/games")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode g : objectMapper.readTree(afterList)) {
            if (g.get("id").asLong() == gameId) {
                assertThat(g.get("maxBetCents").asLong()).isEqualTo(20000);
            }
        }

        // AC2: an audit entry exists with author and before/after snapshots.
        final Map<String, Object> audit = jdbc.queryForMap(
                "SELECT performed_by_user_id, before_value::text AS before_v, after_value::text AS after_v "
                        + "FROM game_commercial_audits WHERE game_id = ? ORDER BY created_at DESC LIMIT 1", gameId);
        assertThat(audit.get("performed_by_user_id")).isNotNull();
        assertThat(audit.get("before_v").toString()).contains("maxBetCents");
        assertThat(audit.get("after_v").toString()).contains("20000");
    }

    // --- AC3: min/step not a multiple of the payline count (5) → 422 ---

    @Test
    void update_betNotMultipleOfPaylines_returns422() throws Exception {
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        final long gameId = fruitsGame(token).get("id").asLong();

        mockMvc.perform(put("/api/v1/operator/games/" + gameId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"minBetCents":501,"maxBetCents":20000,"betStepCents":100,"active":true}"""))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    // --- AC4: a non-operator role cannot use these endpoints → 403 ---

    @Test
    void listGames_asPlayer_returns403() throws Exception {
        final String playerToken = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/operator/games")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }

    // --- AC4 (isolation): a game id that is not the operator's → 404 ---

    @Test
    void update_unknownGame_returns404() throws Exception {
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(put("/api/v1/operator/games/999999")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"minBetCents":100,"maxBetCents":20000,"betStepCents":100,"active":true}"""))
                .andExpect(status().isNotFound());
    }
}
