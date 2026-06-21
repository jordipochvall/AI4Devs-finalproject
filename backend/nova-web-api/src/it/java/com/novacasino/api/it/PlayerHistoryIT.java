package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-14 — player history over a real DB: paginated, ordered newest-first and strictly isolated per
 * player. player1 (who has spun) sees its own movements/rounds; a freshly registered player sees none.
 */
class PlayerHistoryIT extends AbstractIntegrationTest {

    private static final long BET_CENTS = 100L;

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

    private void spin(final String token, final long gameId) throws Exception {
        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk());
    }

    // --- AC1 + AC4: rounds & movements are paginated and ordered created_at DESC ---

    @Test
    void history_isPaginatedAndOrderedDesc() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);
        spin(token, gameId);
        spin(token, gameId);

        // Rounds: standard page wrapper + descending order by createdAt.
        final String roundsJson = mockMvc.perform(get("/api/v1/player/rounds")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.page").value(0))
                .andReturn().getResponse().getContentAsString();
        final JsonNode rounds = objectMapper.readTree(roundsJson).get("content");
        assertThat(rounds.size()).isGreaterThanOrEqualTo(2);
        assertDescending(rounds);

        // Movements: at least the BET of the spins, descending.
        final String txJson = mockMvc.perform(get("/api/v1/player/wallet/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn().getResponse().getContentAsString();
        final JsonNode txs = objectMapper.readTree(txJson).get("content");
        assertThat(txs.size()).isGreaterThanOrEqualTo(1);
        assertDescending(txs);
        boolean hasBet = false;
        for (final JsonNode tx : txs) {
            if ("BET".equals(tx.get("type").asText())) { hasBet = true; break; }
        }
        assertThat(hasBet).isTrue();
    }

    // --- AC2/AC3: a freshly registered player sees none of player1's data ---

    @Test
    void history_isIsolatedPerPlayer() throws Exception {
        // player1 has data (it has spun in other scenarios / this run).
        final String player1 = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        spin(player1, fruitsGameId(player1));

        // A brand-new player with no activity.
        final String newEmail = "it-hist-" + UUID.randomUUID() + "@test.com";
        final String regBody = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"1990-05-20","locale":"es"}"""
                .formatted(newEmail);
        final String regJson = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(regBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        final String newToken = objectMapper.readTree(regJson).get("token").asText();

        // The new player sees nothing — never player1's rounds or movements.
        mockMvc.perform(get("/api/v1/player/rounds")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());

        mockMvc.perform(get("/api/v1/player/wallet/transactions")
                        .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // --- AC5: a non-player role is forbidden ---

    @Test
    void history_asOperator_returns403() throws Exception {
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(get("/api/v1/player/rounds")
                        .header("Authorization", "Bearer " + operator))
                .andExpect(status().isForbidden());
    }

    /** Asserts a content array is ordered by createdAt descending (ties allowed). */
    private void assertDescending(final JsonNode content) {
        OffsetDateTime previous = null;
        for (final JsonNode node : content) {
            final OffsetDateTime current = OffsetDateTime.parse(node.get("createdAt").asText());
            if (previous != null) {
                assertThat(current).isBeforeOrEqualTo(previous);
            }
            previous = current;
        }
    }
}
