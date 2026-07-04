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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Inserts dynamic seed data that requires application-level processing (BCrypt password hashing).
 * Runs once at startup; skips gracefully if users already exist.
 * Static seed (operator + games) is handled by V3__seed.sql (Flyway).
 *
 * <p>The three game math configs are calibrated so their empirical RTP is close to the declared
 * target (HU-31); they live as JSON resources under {@code /seed/*.json} (single source of truth,
 * also read by the RTP regression guard). Free-spins retrigger is disabled so the demo can never
 * hang (the engine supports it; convergence is the mathematician's responsibility, readme §3.3.3).
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
        insertUser(operatorId, "admin@nova.test",    "admin123",    "ADMIN",        "1975-01-01", "es");
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

        // --- Game configs (calibrated, loaded from /seed/*.json) ---
        final Long egyptId  = gameId("egyptian-5x3");
        final Long fruitsId = gameId("fruits-3x3");
        final Long spaceId  = gameId("space-5x3");

        final Long egyptCfg  = insertConfig(egyptId,  mathUserId, 1, readConfig("egyptian"), 0.9500, 8.50,  "Initial Egyptian 5x3 config");
        final Long fruitsCfg = insertConfig(fruitsId, mathUserId, 1, readConfig("fruits"),   0.9200, 3.00,  "Initial Fruits 3x3 config");
        final Long spaceCfg  = insertConfig(spaceId,  mathUserId, 1, readConfig("space"),    0.9650, 12.00, "Initial Space 5x3 config");

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

    /** Loads a calibrated game config JSON from the classpath ({@code /seed/<name>.json}). */
    static String readConfig(final String name) {
        try (var in = SeedDataLoader.class.getResourceAsStream("/seed/" + name + ".json")) {
            return new String(Objects.requireNonNull(in, "seed config not found: " + name).readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalStateException("Cannot read seed config: " + name, e);
        }
    }
}
