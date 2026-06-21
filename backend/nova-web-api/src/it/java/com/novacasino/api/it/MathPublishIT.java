package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * HU-17 — version listing and publication over a real DB. Uses the bounded FRUITS game so a player
 * spin after publishing is safe. Each test creates a fresh version (cloning the active config) and
 * publishes it, so tests stay independent despite the shared, cached context.
 */
class MathPublishIT extends AbstractIntegrationTest {

    private static final long BET_CENTS = 100L;

    @Autowired
    private JdbcTemplate jdbc;

    private JsonNode fruitsGame(final String mathToken) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/math/games")
                        .header("Authorization", "Bearer " + mathToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode g : objectMapper.readTree(json)) {
            if ("FRUITS".equals(g.get("theme").asText())) {
                return g;
            }
        }
        throw new IllegalStateException("Fruits seed game not found");
    }

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

    /** Clones a game's current active config into a brand-new version and returns its id. */
    private long createCloneVersion(final String mathToken, final long gameId, final long fromConfigId)
            throws Exception {
        final String detail = mockMvc.perform(get("/api/v1/math/configs/" + fromConfigId)
                        .header("Authorization", "Bearer " + mathToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final JsonNode cfg = objectMapper.readTree(detail);

        final ObjectNode body = objectMapper.createObjectNode();
        body.set("config", cfg.get("config"));
        body.put("rtpTarget", cfg.get("rtpTarget").asDouble());

        final String created = mockMvc.perform(post("/api/v1/math/games/" + gameId + "/configs")
                        .header("Authorization", "Bearer " + mathToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(created).get("id").asLong();
    }

    private String publishBody(final long configId) {
        return """
                {"configId": %d}""".formatted(configId);
    }

    // --- AC1 + AC2 + AC4: publish moves the active pointer; the next player spin uses it ---

    @Test
    void publish_activatesVersion_andPlayerSpinUsesIt() throws Exception {
        final String mathToken = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        final JsonNode game = fruitsGame(mathToken);
        final long gameId = game.get("id").asLong();
        final long currentActive = game.get("activeConfigId").asLong();

        final long newConfigId = createCloneVersion(mathToken, gameId, currentActive);

        // AC2: publish moves the pointer and records the publication metadata.
        final String published = mockMvc.perform(post("/api/v1/math/games/" + gameId + "/publish")
                        .header("Authorization", "Bearer " + mathToken)
                        .contentType(APPLICATION_JSON).content(publishBody(newConfigId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeConfigId").value(newConfigId))
                .andExpect(jsonPath("$.publishedAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        assertThat(objectMapper.readTree(published).get("version").asInt()).isGreaterThanOrEqualTo(2);

        // AC1: the version list flags the freshly published version as the active one.
        final String list = mockMvc.perform(get("/api/v1/math/games/" + gameId + "/configs")
                        .header("Authorization", "Bearer " + mathToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode v : objectMapper.readTree(list)) {
            assertThat(v.get("active").asBoolean()).isEqualTo(v.get("id").asLong() == newConfigId);
        }

        // AC4: a new player spin persists a round bound to the just-published config.
        final String playerToken = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long playerGameId = fruitsGameId(playerToken);
        final String spin = mockMvc.perform(post("/api/v1/player/games/" + playerGameId + "/spin")
                        .header("Authorization", "Bearer " + playerToken)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        final long roundId = objectMapper.readTree(spin).get("roundId").asLong();

        final Long roundConfigId = jdbc.queryForObject(
                "SELECT game_config_id FROM game_rounds WHERE id = ?", Long.class, roundId);
        assertThat(roundConfigId).isEqualTo(newConfigId);
    }

    // --- AC3: publishing the already-active version → 409 ---

    @Test
    void publish_alreadyActiveVersion_returns409() throws Exception {
        final String mathToken = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        final JsonNode game = fruitsGame(mathToken);
        final long gameId = game.get("id").asLong();

        final long newConfigId = createCloneVersion(mathToken, gameId, game.get("activeConfigId").asLong());

        // Publish it once (now active)…
        mockMvc.perform(post("/api/v1/math/games/" + gameId + "/publish")
                        .header("Authorization", "Bearer " + mathToken)
                        .contentType(APPLICATION_JSON).content(publishBody(newConfigId)))
                .andExpect(status().isOk());

        // …publishing the same (now active) version is a 409 conflict.
        mockMvc.perform(post("/api/v1/math/games/" + gameId + "/publish")
                        .header("Authorization", "Bearer " + mathToken)
                        .contentType(APPLICATION_JSON).content(publishBody(newConfigId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    // --- AC5: a non-math role cannot publish → 403 ---

    @Test
    void publish_asPlayer_returns403() throws Exception {
        final String playerToken = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(playerToken);

        mockMvc.perform(post("/api/v1/math/games/" + gameId + "/publish")
                        .header("Authorization", "Bearer " + playerToken)
                        .contentType(APPLICATION_JSON).content(publishBody(1L)))
                .andExpect(status().isForbidden());
    }
}
