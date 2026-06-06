package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * HU-7-BE-01 — math backoffice: games, version detail and versioned creation.
 * AC1 (creates version N+1 with declared rtpTarget), AC2 (previous version unchanged),
 * AC3 (invalid config → 422 errors[]), AC4 (404), AC5 (MATH_ANALYST role; others 403).
 */
class MathConfigIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private static final String VALID_CONFIG = """
            {
              "grid": { "cols": 3, "rows": 3 },
              "symbols": [
                { "id": "A",  "kind": "REGULAR" },
                { "id": "SC", "kind": "SCATTER" }
              ],
              "reels": [ ["A","SC","A"], ["A","A","SC"], ["A","SC","A"] ],
              "paylines": [ [0,0,0], [1,1,1] ],
              "paytable": [ { "symbol": "A", "payouts": { "3": 5 } } ],
              "scatterPays": { "SC": { "3": 5 } }
            }
            """;

    private String createBody(String configJson, String rtp) {
        return "{\"config\":" + configJson + ",\"rtpTarget\":" + rtp + ",\"notes\":\"IT\"}";
    }

    private long firstGameId(String mathToken) throws Exception {
        String json = mockMvc.perform(get("/api/v1/math/games")
                        .header("Authorization", "Bearer " + mathToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get(0).get("id").asLong();
    }

    // --- AC5/AC1: GET /math/games ---

    @Test
    void games_list_returnsGamesWithActiveVersion() throws Exception {
        String math = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        mockMvc.perform(get("/api/v1/math/games").header("Authorization", "Bearer " + math))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].activeConfigId").exists())
                .andExpect(jsonPath("$[0].activeVersion").value(1));
    }

    // --- AC1/AC2: create a new version ---

    @Test
    void createConfig_valid_createsNextVersion() throws Exception {
        String math = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        long gameId = firstGameId(math);

        // current (active) version of the game
        String gamesJson = mockMvc.perform(get("/api/v1/math/games")
                        .header("Authorization", "Bearer " + math))
                .andReturn().getResponse().getContentAsString();
        JsonNode game0 = objectMapper.readTree(gamesJson).get(0);
        long activeConfigId = game0.get("activeConfigId").asLong();
        int activeVersion = game0.get("activeVersion").asInt();

        String created = mockMvc.perform(post("/api/v1/math/games/" + gameId + "/configs")
                        .header("Authorization", "Bearer " + math)
                        .contentType(APPLICATION_JSON).content(createBody(VALID_CONFIG, "0.95")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value(org.hamcrest.Matchers.greaterThan(activeVersion)))
                .andExpect(jsonPath("$.rtpTarget").value(0.95))
                .andReturn().getResponse().getContentAsString();
        int newVersion = objectMapper.readTree(created).get("version").asInt();
        assertThat(newVersion).isGreaterThan(activeVersion);

        // AC2: the previous active version remains unchanged
        mockMvc.perform(get("/api/v1/math/configs/" + activeConfigId)
                        .header("Authorization", "Bearer " + math))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(activeVersion));
    }

    // --- AC4: version detail + 404 ---

    @Test
    void getConfig_detailAndNotFound() throws Exception {
        String math = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        long activeConfigId = objectMapper.readTree(
                        mockMvc.perform(get("/api/v1/math/games").header("Authorization", "Bearer " + math))
                                .andReturn().getResponse().getContentAsString())
                .get(0).get("activeConfigId").asLong();

        mockMvc.perform(get("/api/v1/math/configs/" + activeConfigId)
                        .header("Authorization", "Bearer " + math))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.config.grid.cols").exists())
                .andExpect(jsonPath("$.rtpTarget").isNumber());

        mockMvc.perform(get("/api/v1/math/configs/999999")
                        .header("Authorization", "Bearer " + math))
                .andExpect(status().isNotFound());
    }

    // --- AC3: invalid config → 422 with errors[] ---

    @Test
    void createConfig_invalid_returns422WithErrors() throws Exception {
        String math = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        long gameId = firstGameId(math);
        // payline with an out-of-range index (row 3 in a 3-row grid)
        String badConfig = VALID_CONFIG.replace("[0,0,0]", "[0,0,3]");

        mockMvc.perform(post("/api/v1/math/games/" + gameId + "/configs")
                        .header("Authorization", "Bearer " + math)
                        .contentType(APPLICATION_JSON).content(createBody(badConfig, "0.95")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").exists());
    }

    // --- AC3: rtpTarget out of [0,1] → 422 ---

    @Test
    void createConfig_rtpOutOfRange_returns422() throws Exception {
        String math = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        long gameId = firstGameId(math);

        mockMvc.perform(post("/api/v1/math/games/" + gameId + "/configs")
                        .header("Authorization", "Bearer " + math)
                        .contentType(APPLICATION_JSON).content(createBody(VALID_CONFIG, "1.5")))
                .andExpect(status().isUnprocessableEntity());
    }

    // --- AC2: the declared rtp_target is persisted as-is (the platform does not modify it) ---

    @Test
    void createConfig_persistsDeclaredRtpUnchanged() throws Exception {
        String math = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        long gameId = firstGameId(math);

        String created = mockMvc.perform(post("/api/v1/math/games/" + gameId + "/configs")
                        .header("Authorization", "Bearer " + math)
                        .contentType(APPLICATION_JSON).content(createBody(VALID_CONFIG, "0.9123")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long newId = objectMapper.readTree(created).get("id").asLong();

        // Read back: the stored rtpTarget is exactly the declared one
        mockMvc.perform(get("/api/v1/math/configs/" + newId)
                        .header("Authorization", "Bearer " + math))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rtpTarget").value(0.9123));
    }

    // --- AC3: immutability — UPDATE/DELETE on game_configs fail due to the trigger ---

    @Test
    void gameConfigs_areImmutable() {
        Long id = jdbc.queryForObject("SELECT id FROM game_configs ORDER BY id LIMIT 1", Long.class);
        assertThatThrownBy(() -> jdbc.update("UPDATE game_configs SET notes = 'hack' WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbc.update("DELETE FROM game_configs WHERE id = ?", id))
                .isInstanceOf(DataAccessException.class);
    }

    // --- AC5: non-MATH_ANALYST role → 403; no token → 401 ---

    @Test
    void games_withOperatorToken_forbidden() throws Exception {
        String op = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(get("/api/v1/math/games").header("Authorization", "Bearer " + op))
                .andExpect(status().isForbidden());
    }

    @Test
    void games_withoutToken_unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/math/games"))
                .andExpect(status().isUnauthorized());
    }
}
