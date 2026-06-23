package com.novacasino.api.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Rate-limiting configuration (anti-bot/anti-abuse). Token-bucket limits applied per endpoint family:
 * login (keyed by client IP) and spin (keyed by authenticated user, or IP if unauthenticated).
 *
 * @param enabled whether the {@link RateLimitFilter} enforces limits (disabled in tests)
 * @param login   bucket for {@code POST /api/v1/auth/login}
 * @param spin    bucket for {@code POST /api/v1/player/games/&#42;/spin}
 */
@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(boolean enabled, Limit login, Limit spin) {

    /**
     * A token-bucket limit: {@code capacity} tokens that refill greedily over
     * {@code refillPeriodSeconds}.
     */
    public record Limit(int capacity, long refillPeriodSeconds) {
    }
}
