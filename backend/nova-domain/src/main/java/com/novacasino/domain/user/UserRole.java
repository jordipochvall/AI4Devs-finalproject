package com.novacasino.domain.user;

/** Platform roles. Persisted as VARCHAR + CHECK in the database (not a native PG enum). */
public enum UserRole {
    PLAYER,
    OPERATOR,
    MATH_ANALYST,
    ADMIN
}
