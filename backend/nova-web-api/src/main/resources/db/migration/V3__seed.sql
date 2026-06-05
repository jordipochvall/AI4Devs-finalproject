-- =============================================================================
-- V3__seed.sql — Datos semilla estáticos (operador + 3 juegos)
--
-- Los usuarios, wallets y game_configs se insertan en SeedDataLoader.java
-- (ApplicationRunner de Spring) porque los passwords requieren BCrypt,
-- que se calcula con el PasswordEncoder de la aplicación, no en SQL.
-- =============================================================================

-- Operador por defecto (single-tenant en MVP)
INSERT INTO operators (code, name, active)
VALUES ('novacasino-default', 'NovaCasino Default', TRUE);

-- 3 juegos — active_config_id = NULL hasta que SeedDataLoader active la config semilla

INSERT INTO games (operator_id, code, name, theme, cover_image_url,
                   min_bet_cents, max_bet_cents, bet_step_cents,
                   allowed_currencies, active, active_config_id)
SELECT id,
       'egyptian-5x3', 'Egipcio', 'EGYPTIAN', '/assets/egyptian/cover.jpg',
       25, 500, 25,                   -- 5 paylines × 5 cts/línea mín | × 100 cts/línea máx
       ARRAY['EUR'::CHAR(3)], TRUE, NULL
FROM   operators WHERE code = 'novacasino-default';

INSERT INTO games (operator_id, code, name, theme, cover_image_url,
                   min_bet_cents, max_bet_cents, bet_step_cents,
                   allowed_currencies, active, active_config_id)
SELECT id,
       'fruits-3x3', 'Frutas Clásico', 'FRUITS', '/assets/fruits/cover.jpg',
       100, 1000, 100,                -- 5 paylines × 20 cts/línea mín | × 200 cts/línea máx
       ARRAY['EUR'::CHAR(3)], TRUE, NULL
FROM   operators WHERE code = 'novacasino-default';

INSERT INTO games (operator_id, code, name, theme, cover_image_url,
                   min_bet_cents, max_bet_cents, bet_step_cents,
                   allowed_currencies, active, active_config_id)
SELECT id,
       'space-5x3', 'Espacial', 'SPACE', '/assets/space/cover.jpg',
       50, 1000, 50,                  -- 10 paylines × 5 cts/línea mín | × 100 cts/línea máx
       ARRAY['EUR'::CHAR(3)], TRUE, NULL
FROM   operators WHERE code = 'novacasino-default';
