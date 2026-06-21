package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
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
 * HU-19 — server-side responsible-gaming enforcement over a real DB. Uses freshly registered players
 * (the gate runs before the balance check, so zero-balance players still hit the gate) to avoid
 * contaminating the seed player shared across the cached context.
 */
class ResponsibleGamingIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    /** Registers a fresh adult player and returns its JWT. */
    private String registerPlayer(final String email) throws Exception {
        final String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"1990-05-20","locale":"es"}"""
                .formatted(email);
        final String json = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
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

    private org.springframework.test.web.servlet.ResultActions spin(final String token, final long gameId) throws Exception {
        return mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(APPLICATION_JSON)
                .content("""
                        {"betCents": 100, "currency": "EUR"}"""));
    }

    // --- AC2: a reached loss limit blocks the spin (422), no round recorded ---

    @Test
    void lossLimitReached_blocksSpin_withNoRound() throws Exception {
        final String email = "it-rg-loss-" + UUID.randomUUID() + "@test.com";
        final String token = registerPlayer(email);
        final long gameId = fruitsGameId(token);

        // A zero loss limit is reached the moment any loss (>= 0) is checked → blocks immediately.
        mockMvc.perform(post("/api/v1/player/limits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"limitType":"LOSS","period":"DAILY","amountCents":0}"""))
                .andExpect(status().isOk());

        spin(token, gameId).andExpect(status().isUnprocessableEntity());

        final Long userId = jdbc.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
        final Long rounds = jdbc.queryForObject(
                "SELECT COUNT(*) FROM game_rounds WHERE player_id = ?", Long.class, userId);
        assertThat(rounds).isZero(); // AC2: no round recorded, no balance effect
    }

    // --- AC2: an active self-exclusion blocks the spin (403) ---

    @Test
    void selfExclusion_blocksSpin() throws Exception {
        final String token = registerPlayer("it-rg-excl-" + UUID.randomUUID() + "@test.com");
        final long gameId = fruitsGameId(token);

        mockMvc.perform(post("/api/v1/player/self-exclusion")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"days": 7}"""))
                .andExpect(status().isOk());

        spin(token, gameId).andExpect(status().isForbidden());
    }

    // --- AC3: hardening immediate, relaxing deferred (echoed in the response) ---

    @Test
    void raisingLimit_isDeferred() throws Exception {
        final String token = registerPlayer("it-rg-cooldown-" + UUID.randomUUID() + "@test.com");

        // Set a low limit, then try to raise it: the response keeps the low value and stages the raise.
        mockMvc.perform(post("/api/v1/player/limits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"limitType":"LOSS","period":"DAILY","amountCents":2000}"""))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/player/limits")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"limitType":"LOSS","period":"DAILY","amountCents":9000}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountCents").value(2000))
                .andExpect(jsonPath("$.pendingAmountCents").value(9000))
                .andExpect(jsonPath("$.pendingEffectiveAt").isNotEmpty());
    }

    // --- AC5: a non-player role cannot manage limits ---

    @Test
    void setLimit_asOperator_returns403() throws Exception {
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(post("/api/v1/player/limits")
                        .header("Authorization", "Bearer " + operator)
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"limitType":"LOSS","period":"DAILY","amountCents":1000}"""))
                .andExpect(status().isForbidden());
    }
}
