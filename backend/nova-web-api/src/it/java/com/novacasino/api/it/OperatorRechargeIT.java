package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-6-BE-01 — player search and idempotent recharge.
 * AC1 (listing with balance), AC2 (recharge increments + movement), AC3 (422/404),
 * AC4 (idempotency), AC5 (OPERATOR role; others 403).
 */
class OperatorRechargeIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private String body(long amountCents) {
        return "{\"amountCents\":" + amountCents + ",\"currency\":\"EUR\"}";
    }

    /** Returns [id, balanceCents] of the first player whose email contains the filter. */
    private long[] firstPlayer(String opToken, String emailFilter) throws Exception {
        String json = mockMvc.perform(get("/api/v1/operator/players")
                        .param("email", emailFilter)
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode first = objectMapper.readTree(json).get("content").get(0);
        return new long[]{ first.get("id").asLong(), first.get("balanceCents").asLong() };
    }

    // --- AC1: paginated listing with balance ---

    @Test
    void listPlayers_returnsPlayersWithBalance() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/operator/players").param("email", "player")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].email").exists())
                .andExpect(jsonPath("$.content[0].balanceCents").exists())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    // --- AC2: recharge increments the balance ---

    @Test
    void recharge_increasesBalance() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        long[] p = firstPlayer(op, "player2@nova.test");
        long id = p[0], before = p[1];

        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON).content(body(5_000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(before + 5_000))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    // --- AC4: same Idempotency-Key → the balance only goes up once ---

    @Test
    void recharge_sameKeyTwice_appliesOnce() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        long[] p = firstPlayer(op, "player3@nova.test");
        long id = p[0], before = p[1];
        String key = UUID.randomUUID().toString();

        // first recharge
        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON).content(body(7_000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(before + 7_000));

        // retry with the same key and payload → same response, no double effect
        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON).content(body(7_000)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(before + 7_000));

        // the final balance only went up by 7000 once
        long after = firstPlayer(op, "player3@nova.test")[1];
        assertThat(after).isEqualTo(before + 7_000);
    }

    // --- AC4: same key with a different payload → 409 ---

    @Test
    void recharge_sameKeyDifferentPayload_conflict() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        long id = firstPlayer(op, "player1@nova.test")[0];
        String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON).content(body(1_000)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", key)
                        .contentType(APPLICATION_JSON).content(body(2_000)))
                .andExpect(status().isConflict());
    }

    // --- AC3 (QA): the RECHARGE movement is recorded in the ledger with the correct operator ---

    @Test
    void recharge_recordsLedgerRowWithOperator() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        Long operatorUserId = jdbc.queryForObject(
                "SELECT id FROM users WHERE email = ?", Long.class, OPERATOR_EMAIL);
        long playerId = firstPlayer(op, "player2@nova.test")[0];

        mockMvc.perform(post("/api/v1/operator/players/" + playerId + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON).content(body(3_000)))
                .andExpect(status().isOk());

        Map<String, Object> row = jdbc.queryForMap("""
                SELECT wt.type, wt.amount_cents, wt.performed_by_user_id, wt.game_round_id
                FROM wallet_transactions wt
                JOIN wallets w ON w.id = wt.wallet_id
                WHERE w.user_id = ?
                ORDER BY wt.id DESC
                LIMIT 1
                """, playerId);

        assertThat(row.get("type")).isEqualTo("RECHARGE");
        assertThat(((Number) row.get("amount_cents")).longValue()).isEqualTo(3_000L);
        assertThat(((Number) row.get("performed_by_user_id")).longValue()).isEqualTo(operatorUserId);
        assertThat(row.get("game_round_id")).isNull();   // recharges carry no round
    }

    // --- AC1 (QA): search pagination ---

    @Test
    void listPlayers_isPaginated() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/operator/players")
                        .param("email", "player").param("page", "0").param("size", "2")
                        .header("Authorization", "Bearer " + op))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalPages").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }

    // --- AC3: amount <= 0 → 422 ---

    @Test
    void recharge_nonPositiveAmount_returns422() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        long id = firstPlayer(op, "player1@nova.test")[0];

        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON).content(body(0)))
                .andExpect(status().isUnprocessableEntity());
    }

    // --- AC3: player not found → 404 ---

    @Test
    void recharge_playerNotFound_returns404() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(post("/api/v1/operator/players/999999/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON).content(body(5_000)))
                .andExpect(status().isNotFound());
    }

    // --- missing Idempotency-Key → 400 ---

    @Test
    void recharge_missingIdempotencyKey_returns400() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        long id = firstPlayer(op, "player1@nova.test")[0];

        mockMvc.perform(post("/api/v1/operator/players/" + id + "/wallet/recharge")
                        .header("Authorization", "Bearer " + op)
                        .contentType(APPLICATION_JSON).content(body(5_000)))
                .andExpect(status().isBadRequest());
    }

    // --- AC5: non-OPERATOR role → 403; no token → 401 ---

    @Test
    void players_withPlayerToken_forbidden() throws Exception {
        String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(get("/api/v1/operator/players").header("Authorization", "Bearer " + player))
                .andExpect(status().isForbidden());
    }

    @Test
    void players_withoutToken_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/operator/players"))
                .andExpect(status().isUnauthorized());
    }
}
