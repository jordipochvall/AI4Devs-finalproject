package com.novacasino.api.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-5-BE-01 — lobby catalogue, game detail (+ config) and wallet.
 * AC1 (active only, no full math in the listing), AC2 (detail + 404),
 * AC3 (balance of the token's player), AC4 (PLAYER role; others 403).
 */
class PlayerCatalogIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    // --- AC1: active games catalogue without exposing the math ---

    @Test
    void games_list_returnsActiveGamesWithoutFullMath() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(get("/api/v1/player/games").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].theme").exists())
                .andExpect(jsonPath("$[0].coverImageUrl").exists())
                .andExpect(jsonPath("$[0].grid.cols").exists())
                // the full math is not exposed in the listing
                .andExpect(jsonPath("$[0].config").doesNotExist())
                .andExpect(jsonPath("$[0].paytable").doesNotExist());
    }

    // --- AC2: detail with the active version's config ---

    @Test
    void game_detail_returnsActiveConfig() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        // take the id of the first game in the catalogue
        String listJson = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long gameId = objectMapper.readTree(listJson).get(0).get("id").asLong();

        mockMvc.perform(get("/api/v1/player/games/" + gameId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.minBetCents").isNumber())
                .andExpect(jsonPath("$.config.grid.cols").exists())
                .andExpect(jsonPath("$.config.symbols").isArray())
                .andExpect(jsonPath("$.config.reels").isArray());
    }

    // --- AC2: non-existent game → 404 ---

    @Test
    void game_detail_notFound_returns404() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(get("/api/v1/player/games/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // --- AC3: balance of the token's player ---

    @Test
    void wallet_returnsAuthenticatedPlayerBalance() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        // Do not assume the exact seed balance: other ITs (recharges) may have mutated it.
        mockMvc.perform(get("/api/v1/player/wallet").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").isNumber())
                .andExpect(jsonPath("$.balanceCents").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    // --- AC2: an inactive game is neither listed nor accessible ---

    @Test
    void inactiveGame_notListedNorAccessible() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        // Insert an inactive game (without an active config) directly into the DB
        jdbc.update("""
                INSERT INTO games (operator_id, code, name, theme, cover_image_url,
                                   min_bet_cents, max_bet_cents, bet_step_cents, active)
                SELECT id, 'it-inactive', 'Inactivo IT', 'SPACE', '/x.jpg', 25, 500, 25, FALSE
                FROM operators WHERE code = 'novacasino-default'
                """);
        Long inactiveId = jdbc.queryForObject(
                "SELECT id FROM games WHERE code = 'it-inactive'", Long.class);
        try {
            // Does not appear in the catalogue
            mockMvc.perform(get("/api/v1/player/games").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.name=='Inactivo IT')]").isEmpty());

            // Not accessible via detail → 404
            mockMvc.perform(get("/api/v1/player/games/" + inactiveId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        } finally {
            jdbc.update("DELETE FROM games WHERE id = ?", inactiveId);
        }
    }

    // --- AC2 (edge): an ACTIVE game without an active config is not playable → 404 ---

    @Test
    void activeGameWithoutActiveConfig_detailReturns404() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        // Active game but with no active_config_id (active_config_id is null by default).
        jdbc.update("""
                INSERT INTO games (operator_id, code, name, theme, cover_image_url,
                                   min_bet_cents, max_bet_cents, bet_step_cents, active)
                SELECT id, 'it-noconfig', 'Sin Config IT', 'SPACE', '/x.jpg', 25, 500, 25, TRUE
                FROM operators WHERE code = 'novacasino-default'
                """);
        Long noConfigId = jdbc.queryForObject(
                "SELECT id FROM games WHERE code = 'it-noconfig'", Long.class);
        try {
            // Listed in the lobby (it is active) but its detail is not playable → 404.
            mockMvc.perform(get("/api/v1/player/games/" + noConfigId)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        } finally {
            jdbc.update("DELETE FROM games WHERE id = ?", noConfigId);
        }
    }

    // --- AC4: other roles → 403; no token → 401 ---

    @Test
    void games_withOperatorToken_forbidden() throws Exception {
        String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(get("/api/v1/player/games").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void wallet_withoutToken_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/player/wallet"))
                .andExpect(status().isUnauthorized());
    }
}
