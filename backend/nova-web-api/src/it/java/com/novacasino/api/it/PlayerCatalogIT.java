package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-5-BE-01 — catálogo del lobby, detalle de juego (+ config) y wallet.
 * AC1 (solo activos, sin matemática completa en el listado), AC2 (detalle + 404),
 * AC3 (saldo del jugador del token), AC4 (rol PLAYER; otros 403).
 */
class PlayerCatalogIT extends AbstractIntegrationTest {

    // --- AC1: catálogo de juegos activos sin exponer la matemática ---

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
                // no se expone la matemática completa en el listado
                .andExpect(jsonPath("$[0].config").doesNotExist())
                .andExpect(jsonPath("$[0].paytable").doesNotExist());
    }

    // --- AC2: detalle con config de la versión activa ---

    @Test
    void game_detail_returnsActiveConfig() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        // tomamos el id del primer juego del catálogo
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

    // --- AC2: juego inexistente → 404 ---

    @Test
    void game_detail_notFound_returns404() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(get("/api/v1/player/games/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // --- AC3: saldo del jugador del token ---

    @Test
    void wallet_returnsAuthenticatedPlayerBalance() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(get("/api/v1/player/wallet").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceCents").value(100000))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    // --- AC4: otros roles → 403; sin token → 401 ---

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
