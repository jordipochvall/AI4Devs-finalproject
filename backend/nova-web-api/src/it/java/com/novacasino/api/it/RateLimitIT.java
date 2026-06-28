package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks that {@link com.novacasino.api.security.RateLimitFilter} is wired into the security
 * chain and returns {@code 429} once a bucket is exhausted — for login (per-IP) and spin (per-user).
 * Uses its own context (rate limiting enabled, tiny capacities) so it does not affect the shared IT
 * context, and pins distinct client IPs per test so the buckets do not collide.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RateLimitIT {

    private static final String EXTERNAL_JDBC_URL = System.getProperty("it.jdbcUrl");
    private static final PostgreSQLContainer<?> POSTGRES;

    static {
        if (EXTERNAL_JDBC_URL == null) {
            POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");
            POSTGRES.start();
        } else {
            POSTGRES = null;
        }
    }

    @DynamicPropertySource
    static void properties(final DynamicPropertyRegistry registry) {
        if (POSTGRES != null) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> EXTERNAL_JDBC_URL);
            registry.add("spring.datasource.username", () -> System.getProperty("it.dbUser", "novacasino"));
            registry.add("spring.datasource.password", () -> System.getProperty("it.dbPass", "novacasino"));
        }
        registry.add("app.jwt.secret", () -> "integration-test-secret-at-least-32-bytes-long");
        registry.add("app.jwt.ttl-seconds", () -> "3600");
        // Rate limiting ON with tiny buckets for this context.
        registry.add("app.rate-limit.enabled", () -> "true");
        registry.add("app.rate-limit.login.capacity", () -> "3");
        registry.add("app.rate-limit.login.refill-period-seconds", () -> "60");
        registry.add("app.rate-limit.spin.capacity", () -> "2");
        registry.add("app.rate-limit.spin.refill-period-seconds", () -> "60");
    }

    /** Seed player credentials (see SeedDataLoader / readme §1.4). */
    private static final String PLAYER_EMAIL = "player1@nova.test";
    private static final String PLAYER_PASS  = "player123";

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    /** Pins the client IP so each test owns an independent per-IP login bucket. */
    private static RequestPostProcessor fromIp(final String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private String loginAndGetToken(final String email, final String password) throws Exception {
        final String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}""".formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }

    private long firstGameId(final String token) throws Exception {
        final String json = mockMvc.perform(get("/api/v1/player/games").header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json).get(0).get("id").asLong();
    }

    @Test
    void login_returns429_afterPerIpBucketExhausted() throws Exception {
        final String body = """
                {"email":"nobody@nova.test","password":"wrong"}""";
        final RequestPostProcessor ip = fromIp("203.0.113.9");

        // Capacity = 3: the first three attempts reach the controller (401 bad credentials)...
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login").with(ip).contentType(APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }

        // ...the fourth is throttled before reaching the controller, with a localised (en) body.
        mockMvc.perform(post("/api/v1/auth/login").with(ip)
                        .header(ACCEPT_LANGUAGE, "en")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.title").value("Too Many Requests"))
                .andExpect(jsonPath("$.detail").value(containsString("rate limit")));
    }

    @Test
    void spin_returns429_afterPerUserBucketExhausted() throws Exception {
        // Login from a fresh IP so the per-IP login bucket is not the one exhausted above.
        final String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        final long gameId = firstGameId(token);
        final String spinBody = """
                {"betCents": 100, "currency": "EUR"}""";

        // Capacity = 2 (per authenticated user): the first two spins are not throttled...
        for (int i = 0; i < 2; i++) {
            final int statusCode = mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                            .header(AUTHORIZATION, "Bearer " + token)
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(APPLICATION_JSON).content(spinBody))
                    .andReturn().getResponse().getStatus();
            assertThat(statusCode).isNotEqualTo(429);
        }

        // ...the third spin by the same user is throttled.
        mockMvc.perform(post("/api/v1/player/games/" + gameId + "/spin")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(APPLICATION_JSON).content(spinBody))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }
}
