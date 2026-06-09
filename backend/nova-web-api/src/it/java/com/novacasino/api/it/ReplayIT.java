package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-3-BE-02 — the deterministic replay endpoint over a real DB: spins to create a round, then the
 * operator fetches the immutable record (AC1/AC2), and checks 404/403 (AC5).
 */
class ReplayIT extends AbstractIntegrationTest {

    /** Spins once as the seed player and returns the produced base round id. */
    private long spinAndGetRoundId() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final String games = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        long fruitsId = -1;
        for (final JsonNode g : objectMapper.readTree(games)) {
            if ("FRUITS".equals(g.get("theme").asText())) { fruitsId = g.get("id").asLong(); }
        }
        final String spin = mockMvc.perform(post("/api/v1/player/games/" + fruitsId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(spin).get("roundId").asLong();
    }

    // --- AC1/AC2: the immutable record is returned with the exact config used ---

    @Test
    void replay_returnsImmutableRecordWithConfig() throws Exception {
        final long roundId = spinAndGetRoundId();
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/operator/rounds/" + roundId + "/replay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roundId").value((int) roundId))
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.gameConfigId").exists())
                .andExpect(jsonPath("$.rngSeed").exists())
                .andExpect(jsonPath("$.result.view").isArray())
                .andExpect(jsonPath("$.result.betCents").value(100))
                .andExpect(jsonPath("$.result.freeSpins").exists())
                // The exact config version's structure is included (context/verification).
                .andExpect(jsonPath("$.config.grid.cols").exists())
                .andExpect(jsonPath("$.config.reels").isArray());
    }

    // --- AC5: unknown round → 404 ---

    @Test
    void replay_unknownRound_notFound() throws Exception {
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(get("/api/v1/operator/rounds/99999999/replay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // --- AC5: a non-OPERATOR role → 403 ---

    @Test
    void replay_withPlayerToken_forbidden() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/operator/rounds/1/replay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
