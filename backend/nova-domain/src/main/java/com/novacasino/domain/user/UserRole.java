package com.novacasino.domain.user;

/** Roles soportados por la plataforma. Mapeados como VARCHAR en BBDD (no ENUM nativo PG). */
public enum UserRole {
    PLAYER,
    OPERATOR,
    MATH_ANALYST
}
