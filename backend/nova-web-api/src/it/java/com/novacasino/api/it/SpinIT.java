package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-1-BE-02 — the spin endpoint over a real DB. Uses the "Frutas Clásico" 3x3 game (no free spins,
 * bounded by construction). Covers a resolved spin and balance reconciliation (AC1/AC2),
 * idempotency replay (AC6), a missing/invalid Idempotency-Key (AC5) and invalid bets (AC4).
 */
class SpinIT extends AbstractIntegrationTest {

    /** The fruits game has 5 paylines and a 100-cent step: 100 is a valid total bet (multiple of both). */
    private static final long BET_CENTS = 100L;

    @Autowired
    private JdbcTemplate jdbc;

    private long fruitsGameId(final String token) throws Exception {
        final String listJson = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode game : objectMapper.readTree(listJson)) {
            if ("FRUITS".equals(game.get("theme").asText())) {
                return game.get("id").asLong();
            }
        }
        throw new IllegalStateException("Fruits seed game not found");
    }

    private long walletBalance(final String token) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/player/wallet")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("balanceCents").asLong();
    }

    // --- AC1 + AC2: a spin resolves and the balance reconciles ---

    @Test
    void spin_resolvesAndReconcilesBalance() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);
        final long pre = walletBalance(token);

        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roundId").isNumber())
                .andExpect(jsonPath("$.betCents").value(BET_CENTS))
                .andExpect(jsonPath("$.lineBetCents").value(BET_CENTS / 5))
                .andExpect(jsonPath("$.view").isArray())
                .andExpect(jsonPath("$.freeSpins.triggered").value(false))
                .andReturn().getResponse().getContentAsString();

        final JsonNode result = objectMapper.readTree(body);
        final long win = result.get("winCents").asLong();
        // pre - bet + win must match both the result and the wallet endpoint.
        final long expectedPost = pre - BET_CENTS + win;
        org.assertj.core.api.Assertions.assertThat(result.get("balancePostCents").asLong())
                .isEqualTo(expectedPost);
        org.assertj.core.api.Assertions.assertThat(walletBalance(token)).isEqualTo(expectedPost);
    }

    // --- AC6: same key + payload replays the original result without a second debit ---

    @Test
    void spin_sameIdempotencyKey_replaysWithoutNewEffect() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);
        final String key = UUID.randomUUID().toString();
        final String payload = """
                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS);

        final String first = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final long afterFirst = walletBalance(token);

        final String second = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(objectMapper.readTree(second).get("roundId").asLong())
                .isEqualTo(objectMapper.readTree(first).get("roundId").asLong());
        // No second debit/credit: balance is unchanged by the replay.
        org.assertj.core.api.Assertions.assertThat(walletBalance(token)).isEqualTo(afterFirst);
    }

    // --- AC2: the spin persists a game_round and a BET ledger movement ---

    @Test
    void spin_persistsRoundAndBetLedger() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);

        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final JsonNode result = objectMapper.readTree(body);
        final long roundId = result.get("roundId").asLong();

        // The base round row exists with the right bet.
        final Long betOfRound = jdbc.queryForObject(
                "SELECT bet_cents FROM game_rounds WHERE id = ?", Long.class, roundId);
        assertThat(betOfRound).isEqualTo(BET_CENTS);

        // The ledger has a BET movement tied to the round (and a WIN iff there was a prize).
        final List<String> types = jdbc.queryForList(
                "SELECT type FROM wallet_transactions WHERE game_round_id = ?", String.class, roundId);
        assertThat(types).contains("BET");
        if (result.get("winCents").asLong() > 0) {
            assertThat(types).contains("WIN");
        }
    }

    // --- AC3 (immutability): game_rounds is append-only; UPDATE is rejected by the DB trigger ---

    @Test
    void gameRound_isImmutable_updateRejected() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);

        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final long roundId = objectMapper.readTree(body).get("roundId").asLong();

        assertThatThrownBy(() ->
                jdbc.update("UPDATE game_rounds SET win_cents = win_cents + 1 WHERE id = ?", roundId))
                .isInstanceOf(DataAccessException.class);
    }

    // --- AC6 (idempotency): same key with a different payload → 409 ---

    @Test
    void spin_sameKeyDifferentPayload_conflict() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);
        final String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isOk());

        // Same key, different bet → 409.
        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 200, "currency": "EUR"}"""))
                .andExpect(status().isConflict());
    }

    // --- AC5: a missing or malformed Idempotency-Key → 400 ---

    @Test
    void spin_withoutIdempotencyKey_badRequest() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);

        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void spin_invalidIdempotencyKey_badRequest() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);

        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "not-a-uuid")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isBadRequest());
    }

    // --- AC4: bet out of range / not a valid multiple → 422 ---

    @Test
    void spin_betOutOfRange_unprocessable() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);

        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 999999, "currency": "EUR"}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void spin_betNotMultipleOfStep_unprocessable() throws Exception {
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(token);

        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 150, "currency": "EUR"}"""))
                .andExpect(status().isUnprocessableEntity());
    }
}
