-- =============================================================================
-- V11__commercial_game_names.sql — Commercial (marketing) names for the seed games.
-- Cosmetic rename of the three demo games to catchy titles chosen with the product
-- owner. The `games` table carries no immutability trigger (only audit/append-only
-- tables do), so a plain UPDATE is safe. Applied on top of V3__seed.sql, so both the
-- existing database and fresh installs end up with these names.
-- =============================================================================

UPDATE games SET name = 'Tesoro del Nilo' WHERE code = 'egyptian-5x3';
UPDATE games SET name = 'Fruti Fiesta'    WHERE code = 'fruits-3x3';
UPDATE games SET name = 'Nova Cósmica'    WHERE code = 'space-5x3';
