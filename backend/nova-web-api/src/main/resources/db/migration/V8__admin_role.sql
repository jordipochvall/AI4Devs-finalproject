-- =============================================================================
-- V8__admin_role.sql — Rol ADMIN para la gestión multi-operador (HU-25)
-- Aditiva: amplía el dominio de users.role. No modifica filas existentes.
-- =============================================================================

ALTER TABLE users DROP CONSTRAINT users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('PLAYER','OPERATOR','MATH_ANALYST','ADMIN'));
