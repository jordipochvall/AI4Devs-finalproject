package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-3-QA-01 — safety net for the "faithful replay" + immutability promise (DGOJ). Covers audit
 * filtering/ordering by player (AC complement to AuditIT), the byte-for-byte reproducibility of the
 * replay (AC2), its resilience to engine drift — it renders the stored record, never recomputes
 * (AC3) — and the append-only immutability of {@code game_rounds} (UPDATE/DELETE rejected).
 */
class AuditReplayQaIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    /** Spins once as the seed player; returns the spin response JSON. */
    private JsonNode spin() throws Exception {
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
        return objectMapper.readTree(spin);
    }

    private long playerId(final String operatorToken) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/operator/players?email=player1@nova.test")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("content").get(0).get("id").asLong();
    }

    // --- AC: audit filtered by player, newest first ---

    @Test
    void audit_filterByPlayer_orderedNewestFirst() throws Exception {
        spin();
        spin();
        final String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        final long pid = playerId(op);

        final String json = mockMvc.perform(get("/api/v1/operator/rounds?playerId=" + pid + "&size=50")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].playerId").value((int) pid))
                .andReturn().getResponse().getContentAsString();

        final JsonNode content = objectMapper.readTree(json).get("content");
        assertThat(content.size()).isGreaterThanOrEqualTo(2);
        // created_at DESC: each row is not newer than the previous one.
        for (int i = 1; i < content.size(); i++) {
            final String prev = content.get(i - 1).get("createdAt").asText();
            final String cur = content.get(i).get("createdAt").asText();
            assertThat(prev.compareTo(cur)).isGreaterThanOrEqualTo(0);
        }
    }

    // --- AC: date-range filter ---

    @Test
    void audit_dateRangeFilter_excludesFutureWindow() throws Exception {
        spin();
        final String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        // A window entirely in the future → no rounds.
        mockMvc.perform(get("/api/v1/operator/rounds?from=2999-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        // A window since the epoch → there is at least one round.
        mockMvc.perform(get("/api/v1/operator/rounds?from=2000-01-01T00:00:00Z")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }

    // --- AC2: the same replay request returns a byte-for-byte identical response ---

    @Test
    void replay_isReproducibleByteForByte() throws Exception {
        final long roundId = spin().get("roundId").asLong();
        final String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        final String first = mockMvc.perform(get("/api/v1/operator/rounds/" + roundId + "/replay")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        final String second = mockMvc.perform(get("/api/v1/operator/rounds/" + roundId + "/replay")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(second).isEqualTo(first);
    }

    // --- AC3: the replay renders the stored record (not a recomputation) ---

    @Test
    void replay_returnsStoredRecord_notRecomputed() throws Exception {
        final JsonNode spin = spin();
        final long roundId = spin.get("roundId").asLong();
        final String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        final String replay = mockMvc.perform(get("/api/v1/operator/rounds/" + roundId + "/replay")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        final JsonNode result = objectMapper.readTree(replay).get("result");

        // The replayed view/win equal exactly what the player got at spin time → it is the record.
        assertThat(result.get("view")).isEqualTo(spin.get("view"));
        assertThat(result.get("winCents").asLong()).isEqualTo(spin.get("winCents").asLong());
    }

    // --- Immutability: UPDATE and DELETE on game_rounds are rejected by the trigger ---

    @Test
    void gameRounds_areAppendOnly() throws Exception {
        final long roundId = spin().get("roundId").asLong();

        assertThatThrownBy(() ->
                jdbc.update("UPDATE game_rounds SET win_cents = win_cents + 1 WHERE id = ?", roundId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() ->
                jdbc.update("DELETE FROM game_rounds WHERE id = ?", roundId))
                .isInstanceOf(DataAccessException.class);
    }
}
