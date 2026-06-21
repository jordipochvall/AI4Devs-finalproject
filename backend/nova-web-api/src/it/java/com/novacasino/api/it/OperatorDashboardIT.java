package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-16 — operator dashboard over a real DB. Aggregates are checked against a direct SQL reference
 * (robust to whatever data the shared context holds); the date filter, round detail and role/404 paths
 * are covered too.
 */
class OperatorDashboardIT extends AbstractIntegrationTest {

    private static final long BET_CENTS = 100L;

    @Autowired
    private JdbcTemplate jdbc;

    private long fruitsGameId(final String token) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode g : objectMapper.readTree(json)) {
            if ("FRUITS".equals(g.get("theme").asText())) {
                return g.get("id").asLong();
            }
        }
        throw new IllegalStateException("Fruits seed game not found");
    }

    private long spin(final String token, final long gameId) throws Exception {
        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("roundId").asLong();
    }

    // --- AC1 + AC3: aggregates match a direct SQL reference for the operator ---

    @Test
    void dashboard_aggregatesMatchSqlReference() throws Exception {
        final String operatorToken = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        final String playerToken = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        spin(playerToken, fruitsGameId(playerToken)); // ensure there is activity

        final Long operatorId = jdbc.queryForObject(
                "SELECT id FROM operators WHERE code = 'novacasino-default'", Long.class);
        final Long refActive = jdbc.queryForObject(
                "SELECT COUNT(DISTINCT player_id) FROM game_rounds WHERE operator_id = ?", Long.class, operatorId);
        final Long refGgr = jdbc.queryForObject(
                "SELECT COALESCE(SUM(bet_cents),0) - COALESCE(SUM(win_cents),0) FROM game_rounds WHERE operator_id = ?",
                Long.class, operatorId);
        final Long refRounds = jdbc.queryForObject(
                "SELECT COUNT(*) FROM game_rounds WHERE operator_id = ?", Long.class, operatorId);

        final String json = mockMvc.perform(get("/api/v1/operator/dashboard")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final JsonNode dash = objectMapper.readTree(json);

        assertThat(dash.get("activePlayers").asLong()).isEqualTo(refActive);
        assertThat(dash.get("ggrCents").asLong()).isEqualTo(refGgr);
        assertThat(dash.get("totalRounds").asLong()).isEqualTo(refRounds);
        assertThat(dash.get("topGames")).isNotEmpty();
    }

    // --- AC2: a future date window excludes all activity ---

    @Test
    void dashboard_dateFilterExcludesOutOfWindow() throws Exception {
        final String operatorToken = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/operator/dashboard")
                        .header("Authorization", "Bearer " + operatorToken)
                        .param("from", "2999-01-01T00:00:00Z")
                        .param("to", "2999-12-31T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activePlayers").value(0))
                .andExpect(jsonPath("$.totalRounds").value(0))
                .andExpect(jsonPath("$.ggrCents").value(0))
                .andExpect(jsonPath("$.topGames").isEmpty());
    }

    // --- AC2 (BE): round detail returns amounts and the symbol grid ---

    @Test
    void roundDetail_returnsAmountsAndView() throws Exception {
        final String operatorToken = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        final String playerToken = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long roundId = spin(playerToken, fruitsGameId(playerToken));

        mockMvc.perform(get("/api/v1/operator/rounds/" + roundId)
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roundId").value(roundId))
                .andExpect(jsonPath("$.betCents").value(BET_CENTS))
                .andExpect(jsonPath("$.view").isArray());
    }

    // --- AC4: unknown round → 404; non-operator role → 403 ---

    @Test
    void roundDetail_unknown_returns404() throws Exception {
        final String operatorToken = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(get("/api/v1/operator/rounds/999999")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void dashboard_asPlayer_returns403() throws Exception {
        final String playerToken = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/operator/dashboard")
                        .header("Authorization", "Bearer " + playerToken))
                .andExpect(status().isForbidden());
    }
}
