package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for integration tests: a real Postgres 18, with Flyway migrations and seed
 * (SeedDataLoader) applied on context startup. The context is cached across IT classes that
 * share this configuration.
 *
 * <p>By default it starts a container via <b>Testcontainers</b> (standard CI mode). If the
 * {@code it.jdbcUrl} system property is provided, the tests run against an already-running
 * <b>external database</b> (useful for debugging in environments where the JVM cannot open the
 * Docker socket, e.g. Docker Desktop over a named pipe).
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    private static final String EXTERNAL_JDBC_URL = System.getProperty("it.jdbcUrl");
    static final PostgreSQLContainer<?> POSTGRES;

    static {
        if (EXTERNAL_JDBC_URL == null) {
            POSTGRES = new PostgreSQLContainer<>("postgres:18-alpine");
            POSTGRES.start();
        } else {
            POSTGRES = null; // use the external database given by it.jdbcUrl
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        if (POSTGRES != null) {
            registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        } else {
            registry.add("spring.datasource.url", () -> EXTERNAL_JDBC_URL);
            registry.add("spring.datasource.username", () -> System.getProperty("it.dbUser", "novacasino"));
            registry.add("spring.datasource.password", () -> System.getProperty("it.dbPass", "novacasino"));
        }
        // JWT secret required to start the context in tests
        registry.add("app.jwt.secret", () -> "integration-test-secret-at-least-32-bytes-long");
        registry.add("app.jwt.ttl-seconds", () -> "3600");
        // Rate limiting off in ITs: they share one cached context and perform many logins/spins
        // from localhost, which the per-IP/per-user buckets would otherwise throttle. The filter
        // itself is covered by RateLimitFilterTest.
        registry.add("app.rate-limit.enabled", () -> "false");
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    /** Seed credentials (see SeedDataLoader / readme §1.4). */
    protected static final String OPERATOR_EMAIL = "operator@nova.test";
    protected static final String OPERATOR_PASS  = "operator123";
    protected static final String MATH_EMAIL     = "math@nova.test";
    protected static final String MATH_PASS      = "math123";
    protected static final String PLAYER_EMAIL   = "player1@nova.test";
    protected static final String PLAYER_PASS    = "player123";

    /** Logs in with a seed user and returns their JWT. */
    protected String loginAndGetToken(final String email, final String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("token").asText();
    }
}
