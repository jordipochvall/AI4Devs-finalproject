package com.novacasino.api.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end check that the {@link com.novacasino.api.security.RateLimitFilter} is wired into the
 * security chain and returns {@code 429} once the per-IP login bucket is exhausted. Uses its own
 * context (rate limiting enabled, tiny login capacity) so it does not affect the shared IT context.
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
        // Rate limiting ON with a tiny login bucket for this context.
        registry.add("app.rate-limit.enabled", () -> "true");
        registry.add("app.rate-limit.login.capacity", () -> "3");
        registry.add("app.rate-limit.login.refill-period-seconds", () -> "60");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void login_returns429_afterBucketExhausted() throws Exception {
        final String body = """
                {"email":"nobody@nova.test","password":"wrong"}""";

        // Capacity = 3: the first three attempts reach the controller (401 bad credentials)...
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON).content(body))
                    .andExpect(status().isUnauthorized());
        }

        // ...the fourth is throttled by the filter before reaching the controller.
        mockMvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(header().string("Content-Type", containsString("application/problem+json")));
    }
}
