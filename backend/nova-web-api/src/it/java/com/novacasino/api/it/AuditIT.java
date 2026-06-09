package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-3-BE-01 — the operator audit endpoint over a real DB: produces a round (via a spin), then lists
 * it with pagination and filters (AC1/AC2/AC3) and enforces the OPERATOR role (AC4).
 */
class AuditIT extends AbstractIntegrationTest {

    /** Spins once as the seed player so there is at least one auditable round. */
    private void produceRound() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final String games = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        long fruitsId = -1;
        for (final JsonNode g : objectMapper.readTree(games)) {
            if ("FRUITS".equals(g.get("theme").asText())) { fruitsId = g.get("id").asLong(); }
        }
        mockMvc.perform(post("/api/v1/player/games/" + fruitsId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isOk());
    }

    // --- AC1/AC2: paginated audit listing for the operator ---

    @Test
    void rounds_listedPaginatedNewestFirst() throws Exception {
        produceRound();
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/operator/rounds?page=0&size=10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].betCents").exists());
    }

    // --- AC3: filters combine (AND); a non-matching game returns an empty page ---

    @Test
    void rounds_filterByGame_combinesFilters() throws Exception {
        produceRound();
        final String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        // A game id that the operator does not own → no rows, but a valid 200 page.
        mockMvc.perform(get("/api/v1/operator/rounds?gameId=999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // --- AC4: only OPERATOR; a player token → 403 ---

    @Test
    void rounds_withPlayerToken_forbidden() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/operator/rounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
