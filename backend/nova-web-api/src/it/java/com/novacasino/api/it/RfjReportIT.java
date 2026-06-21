package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-21 — RFJ regulatory report over a real DB: aggregates for the current month, the integrity
 * stamp, the block on tampered data and the role guard.
 */
class RfjReportIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    private final int year  = OffsetDateTime.now(ZoneOffset.UTC).getYear();
    private final int month = OffsetDateTime.now(ZoneOffset.UTC).getMonthValue();

    private long spin(final String token, final long gameId) throws Exception {
        final String body = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"betCents": 100, "currency": "EUR"}"""))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("roundId").asLong();
    }

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

    private String reqBody() {
        return """
                {"year": %d, "month": %d}""".formatted(year, month);
    }

    // --- AC1 + AC2: report aggregates the period and stamps an intact integrity status ---

    @Test
    void generate_intactPeriod_returnsAggregates() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        spin(player, fruitsGameId(player));
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        mockMvc.perform(post("/api/v1/operator/reports/rfj")
                        .header("Authorization", "Bearer " + operator)
                        .contentType(APPLICATION_JSON).content(reqBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRounds").isNumber())
                .andExpect(jsonPath("$.totalWageredCents").isNumber())
                .andExpect(jsonPath("$.integrityConsistent").value(true));
    }

    // --- AC3: a tampered period blocks report generation ---

    @Test
    void generate_tamperedPeriod_isBlocked() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long roundId = spin(player, fruitsGameId(player));
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);

        final long originalWin = jdbc.queryForObject(
                "SELECT win_cents FROM game_rounds WHERE id = ?", Long.class, roundId);

        jdbc.execute("ALTER TABLE game_rounds DISABLE TRIGGER trg_game_rounds_no_update_delete");
        try {
            jdbc.update("UPDATE game_rounds SET win_cents = win_cents + 1 WHERE id = ?", roundId);

            mockMvc.perform(post("/api/v1/operator/reports/rfj")
                            .header("Authorization", "Bearer " + operator)
                            .contentType(APPLICATION_JSON).content(reqBody()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.firstBrokenRoundId").value(roundId));

            jdbc.update("UPDATE game_rounds SET win_cents = ? WHERE id = ?", originalWin, roundId);
        } finally {
            jdbc.execute("ALTER TABLE game_rounds ENABLE TRIGGER trg_game_rounds_no_update_delete");
        }
    }

    // --- AC4: a non-operator role cannot generate the report ---

    @Test
    void generate_asPlayer_returns403() throws Exception {
        final String player = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        mockMvc.perform(post("/api/v1/operator/reports/rfj")
                        .header("Authorization", "Bearer " + player)
                        .contentType(APPLICATION_JSON).content(reqBody()))
                .andExpect(status().isForbidden());
    }
}
