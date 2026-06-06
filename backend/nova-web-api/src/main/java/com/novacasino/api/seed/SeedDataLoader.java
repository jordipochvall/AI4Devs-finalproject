package com.novacasino.api.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts dynamic seed data that requires application-level processing (BCrypt password hashing).
 * Runs once at startup; skips gracefully if users already exist.
 * Static seed (operator + games) is handled by V3__seed.sql (Flyway).
 */
@Component
public class SeedDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    private final JdbcTemplate jdbc;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public SeedDataLoader(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(final ApplicationArguments args) {
        final Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (count != null && count > 0) {
            log.info("Seed users already present, skipping SeedDataLoader.");
            return;
        }

        log.info("Inserting seed users, wallets and game configs...");

        final Long operatorId = jdbc.queryForObject(
                "SELECT id FROM operators WHERE code = 'novacasino-default'", Long.class);

        // --- Users ---
        insertUser(operatorId, "operator@nova.test", "operator123", "OPERATOR",     "1980-01-01", "es");
        final Long mathUserId = insertUser(operatorId,
                "math@nova.test",    "math123",     "MATH_ANALYST", "1985-06-15", "es");
        final Long p1 = insertUser(operatorId, "player1@nova.test", "player123", "PLAYER", "1990-03-20", "es");
        final Long p2 = insertUser(operatorId, "player2@nova.test", "player123", "PLAYER", "1988-07-12", "es");
        final Long p3 = insertUser(operatorId, "player3@nova.test", "player123", "PLAYER", "1995-11-30", "en");

        // --- Wallets for players (1,000 EUR = 100,000 cents) ---
        insertWallet(operatorId, p1, 100_000L);
        insertWallet(operatorId, p2, 100_000L);
        insertWallet(operatorId, p3, 100_000L);

        // --- Game configs ---
        final Long egyptId  = gameId("egyptian-5x3");
        final Long fruitsId = gameId("fruits-3x3");
        final Long spaceId  = gameId("space-5x3");

        final Long egyptCfg  = insertConfig(egyptId,  mathUserId, 1, EGYPTIAN_CONFIG, 0.9500, 8.50,  "Initial Egyptian 5x3 config");
        final Long fruitsCfg = insertConfig(fruitsId, mathUserId, 1, FRUITS_CONFIG,   0.9200, 3.00,  "Initial Fruits 3x3 config");
        final Long spaceCfg  = insertConfig(spaceId,  mathUserId, 1, SPACE_CONFIG,    0.9650, 12.00, "Initial Space 5x3 config");

        // --- Activate configs ---
        activateConfig(egyptId,  egyptCfg);
        activateConfig(fruitsId, fruitsCfg);
        activateConfig(spaceId,  spaceCfg);

        log.info("Seed complete: operator={}, users=5, wallets=3, games=3, configs=3.", operatorId);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Long insertUser(final Long operatorId, final String email, final String plainPassword,
                            final String role, final String birthDate, final String locale) {
        return jdbc.queryForObject(
                """
                INSERT INTO users (operator_id, email, password_hash, role, birth_date, locale)
                VALUES (?, ?, ?, ?, ?::DATE, ?)
                RETURNING id
                """,
                Long.class,
                operatorId, email, passwordEncoder.encode(plainPassword), role, birthDate, locale);
    }

    private void insertWallet(final Long operatorId, final Long userId, final long balanceCents) {
        jdbc.update(
                """
                INSERT INTO wallets (operator_id, user_id, balance_cents, currency)
                VALUES (?, ?, ?, 'EUR')
                """,
                operatorId, userId, balanceCents);
    }

    private Long gameId(final String code) {
        return jdbc.queryForObject("SELECT id FROM games WHERE code = ?", Long.class, code);
    }

    private Long insertConfig(final Long gameId, final Long createdBy, final int version,
                              final String configJson, final double rtpTarget,
                              final double volatilityTarget, final String notes) {
        return jdbc.queryForObject(
                """
                INSERT INTO game_configs (game_id, version, config, rtp_target, volatility_target,
                                         created_by_user_id, notes)
                VALUES (?, ?, ?::JSONB, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                gameId, version, configJson, rtpTarget, volatilityTarget, createdBy, notes);
    }

    private void activateConfig(final Long gameId, final Long configId) {
        jdbc.update("UPDATE games SET active_config_id = ? WHERE id = ?", configId, gameId);
    }

    // =========================================================================
    // JSON configs of the three seed games
    // =========================================================================

    private static final String EGYPTIAN_CONFIG = """
            {
              "grid": { "cols": 5, "rows": 3 },
              "symbols": [
                { "id": "WILD",    "kind": "WILD" },
                { "id": "SCATTER", "kind": "SCATTER" },
                { "id": "ANUBIS",  "kind": "REGULAR" },
                { "id": "SCARAB",  "kind": "REGULAR" },
                { "id": "A",       "kind": "REGULAR" }
              ],
              "reels": [
                ["ANUBIS","A","SCARAB","WILD","A","SCATTER","SCARAB","A","ANUBIS","SCARAB","A","WILD"],
                ["A","SCARAB","ANUBIS","A","WILD","SCARAB","A","SCATTER","ANUBIS","A","SCARAB","WILD"],
                ["SCARAB","A","ANUBIS","SCATTER","A","WILD","SCARAB","A","ANUBIS","A","SCARAB","SCATTER"],
                ["A","ANUBIS","SCARAB","A","WILD","A","SCATTER","SCARAB","A","ANUBIS","WILD","SCARAB"],
                ["SCARAB","A","SCATTER","ANUBIS","A","SCARAB","WILD","A","ANUBIS","SCATTER","A","SCARAB"]
              ],
              "paylines": [
                [1,1,1,1,1],
                [0,0,0,0,0],
                [2,2,2,2,2],
                [0,1,2,1,0],
                [2,1,0,1,2]
              ],
              "paytable": [
                { "symbol": "ANUBIS", "payouts": { "3": 10, "4": 50,  "5": 250 } },
                { "symbol": "SCARAB", "payouts": { "3": 5,  "4": 20,  "5": 100 } },
                { "symbol": "A",      "payouts": { "3": 2,  "4": 10,  "5": 40  } }
              ],
              "scatterPays": {
                "SCATTER": { "2": 1, "3": 5, "4": 20, "5": 100 }
              },
              "bonus": {
                "wild": { "substitutes": ["REGULAR"] },
                "freeSpins": {
                  "triggerSymbol": "SCATTER",
                  "minTriggerCount": 3,
                  "award": { "3": 8, "4": 12, "5": 20 },
                  "multiplier": 2,
                  "retrigger": true
                }
              }
            }
            """;

    private static final String FRUITS_CONFIG = """
            {
              "grid": { "cols": 3, "rows": 3 },
              "symbols": [
                { "id": "SEVEN",  "kind": "REGULAR" },
                { "id": "BAR3",   "kind": "REGULAR" },
                { "id": "BAR2",   "kind": "REGULAR" },
                { "id": "BAR",    "kind": "REGULAR" },
                { "id": "CHERRY", "kind": "REGULAR" },
                { "id": "LEMON",  "kind": "REGULAR" },
                { "id": "ORANGE", "kind": "REGULAR" },
                { "id": "PLUM",   "kind": "REGULAR" }
              ],
              "reels": [
                ["SEVEN","BAR3","BAR2","CHERRY","BAR","LEMON","ORANGE","PLUM","BAR2","CHERRY","BAR","LEMON","ORANGE","PLUM","BAR2"],
                ["CHERRY","PLUM","LEMON","ORANGE","BAR","BAR2","BAR3","SEVEN","CHERRY","PLUM","LEMON","ORANGE","BAR","BAR2","BAR3"],
                ["LEMON","ORANGE","CHERRY","PLUM","BAR","BAR2","BAR3","SEVEN","LEMON","ORANGE","CHERRY","PLUM","BAR","BAR2","SEVEN"]
              ],
              "paylines": [
                [1,1,1],
                [0,0,0],
                [2,2,2],
                [0,1,2],
                [2,1,0]
              ],
              "paytable": [
                { "symbol": "SEVEN",  "payouts": { "3": 100 } },
                { "symbol": "BAR3",   "payouts": { "3": 50  } },
                { "symbol": "BAR2",   "payouts": { "3": 25  } },
                { "symbol": "BAR",    "payouts": { "2": 1,  "3": 10 } },
                { "symbol": "CHERRY", "payouts": { "2": 2,  "3": 5  } },
                { "symbol": "LEMON",  "payouts": { "2": 1,  "3": 3  } },
                { "symbol": "ORANGE", "payouts": { "2": 1,  "3": 3  } },
                { "symbol": "PLUM",   "payouts": { "2": 1,  "3": 3  } }
              ],
              "bonus": {}
            }
            """;

    private static final String SPACE_CONFIG = """
            {
              "grid": { "cols": 5, "rows": 3 },
              "symbols": [
                { "id": "WILD",    "kind": "WILD" },
                { "id": "SCATTER", "kind": "SCATTER" },
                { "id": "PLANET",  "kind": "REGULAR" },
                { "id": "COMET",   "kind": "REGULAR" },
                { "id": "STAR",    "kind": "REGULAR" },
                { "id": "K",       "kind": "REGULAR" },
                { "id": "A",       "kind": "REGULAR" }
              ],
              "reels": [
                ["PLANET","A","COMET","WILD","A","SCATTER","COMET","A","STAR","K","PLANET","A"],
                ["A","COMET","PLANET","A","WILD","COMET","A","SCATTER","K","STAR","A","PLANET"],
                ["COMET","A","PLANET","SCATTER","A","WILD","COMET","A","STAR","K","COMET","SCATTER"],
                ["A","PLANET","COMET","A","WILD","A","SCATTER","COMET","K","STAR","PLANET","A"],
                ["COMET","A","SCATTER","PLANET","A","COMET","WILD","A","K","STAR","COMET","SCATTER"]
              ],
              "paylines": [
                [1,1,1,1,1],
                [0,0,0,0,0],
                [2,2,2,2,2],
                [0,1,2,1,0],
                [2,1,0,1,2],
                [0,0,1,2,2],
                [2,2,1,0,0],
                [1,0,0,0,1],
                [1,2,2,2,1],
                [0,1,0,1,0]
              ],
              "paytable": [
                { "symbol": "PLANET", "payouts": { "3": 12, "4": 60,  "5": 300 } },
                { "symbol": "COMET",  "payouts": { "3": 8,  "4": 40,  "5": 200 } },
                { "symbol": "STAR",   "payouts": { "3": 5,  "4": 25,  "5": 125 } },
                { "symbol": "K",      "payouts": { "3": 3,  "4": 15,  "5": 60  } },
                { "symbol": "A",      "payouts": { "3": 2,  "4": 10,  "5": 40  } }
              ],
              "scatterPays": {
                "SCATTER": { "2": 1, "3": 5, "4": 20, "5": 100 }
              },
              "bonus": {
                "wild": { "substitutes": ["REGULAR"] },
                "freeSpins": {
                  "triggerSymbol": "SCATTER",
                  "minTriggerCount": 3,
                  "award": { "3": 10, "4": 15, "5": 25 },
                  "multiplier": 3,
                  "retrigger": true
                }
              }
            }
            """;
}
