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
 * HU-26 — progressive jackpot over a real DB. The pool is attached to the FRUITS game only for the
 * duration of each test and removed afterwards, so other ITs that spin FRUITS are unaffected.
 */
class JackpotIT extends AbstractIntegrationTest {

    private static final long BET_CENTS = 100L;
    private static final long SEED_CENTS = 100_000L;

    @Autowired
    private JdbcTemplate jdbc;

    private long fruitsGameId(final String token) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/player/games")
                        .header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        for (final JsonNode g : objectMapper.readTree(json)) {
            if ("FRUITS".equals(g.get("theme").asText())) {
                return g.get("id").asLong();
            }
        }
        throw new IllegalStateException("Fruits seed game not found");
    }

    /** Upserts a jackpot pool for the game with the given odds (1 = always award). */
    private void setPool(final long gameId, final long oddsDenominator) {
        jdbc.update("""
                INSERT INTO jackpot_pools (game_id, current_cents, seed_cents, contribution_bps, odds_denominator)
                VALUES (?, ?, ?, 100, ?)
                ON CONFLICT (game_id) DO UPDATE SET current_cents = EXCLUDED.current_cents,
                    seed_cents = EXCLUDED.seed_cents, contribution_bps = EXCLUDED.contribution_bps,
                    odds_denominator = EXCLUDED.odds_denominator""",
                gameId, SEED_CENTS, SEED_CENTS, oddsDenominator);
    }

    private long spin(final String token, final long gameId) throws Exception {
        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": %d, "currency": "EUR"}""".formatted(BET_CENTS)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("roundId").asLong();
    }

    // --- AC1 + AC3: a bet contributes; an awarded spin pays the pool and resets it to the seed ---

    @Test
    void award_paysPoolAndResets() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(player);
        setPool(gameId, 1L); // odds 1 → always awarded
        try {
            final long roundId = spin(player, gameId);

            // A grant tied to the round exists for at least the pool + contribution.
            final Long grantAmount = jdbc.queryForObject(
                    "SELECT amount_cents FROM jackpot_grants WHERE game_round_id = ?", Long.class, roundId);
            assertThat(grantAmount).isGreaterThanOrEqualTo(SEED_CENTS);

            // The pool is reset to its seed after the award.
            final Long current = jdbc.queryForObject(
                    "SELECT current_cents FROM jackpot_pools WHERE game_id = ?", Long.class, gameId);
            assertThat(current).isEqualTo(SEED_CENTS);

            // The round's win includes the jackpot (>= pool seed).
            final Long roundWin = jdbc.queryForObject(
                    "SELECT win_cents FROM game_rounds WHERE id = ?", Long.class, roundId);
            assertThat(roundWin).isGreaterThanOrEqualTo(SEED_CENTS);
        } finally {
            jdbc.update("DELETE FROM jackpot_pools WHERE game_id = ?", gameId);
        }
    }

    // --- AC1: a non-awarding spin only grows the pool by the contribution, no grant ---

    @Test
    void noAward_growsPoolByContribution() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = fruitsGameId(player);
        setPool(gameId, 1_000_000_000L); // odds 1e9 → effectively never awarded
        try {
            final long roundId = spin(player, gameId);

            final Integer grants = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM jackpot_grants WHERE game_round_id = ?", Integer.class, roundId);
            assertThat(grants).isZero();

            // Contribution = 100 * 100 / 10000 = 1 cent → pool grew from seed by 1.
            final Long current = jdbc.queryForObject(
                    "SELECT current_cents FROM jackpot_pools WHERE game_id = ?", Long.class, gameId);
            assertThat(current).isEqualTo(SEED_CENTS + 1L);
        } finally {
            jdbc.update("DELETE FROM jackpot_pools WHERE game_id = ?", gameId);
        }
    }
}
