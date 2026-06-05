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
 * Base de los tests de integración: Postgres 18 real, migraciones Flyway y seed
 * (SeedDataLoader) aplicados al arrancar el contexto. El contexto se cachea entre
 * clases IT que comparten esta config.
 *
 * <p>Por defecto levanta un contenedor con <b>Testcontainers</b> (modo CI estándar).
 * Si se proporciona la propiedad de sistema {@code it.jdbcUrl}, los tests se ejecutan
 * contra una <b>BBDD externa</b> ya en marcha (útil para depurar en entornos donde la
 * JVM no puede abrir el socket de Docker, p. ej. Docker Desktop por named pipe).
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
            POSTGRES = null; // se usa la BBDD externa indicada por it.jdbcUrl
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
        // Secreto JWT necesario para arrancar el contexto en tests
        registry.add("app.jwt.secret", () -> "integration-test-secret-at-least-32-bytes-long");
        registry.add("app.jwt.ttl-seconds", () -> "3600");
    }

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    /** Credenciales semilla (ver SeedDataLoader / readme §1.4). */
    protected static final String OPERATOR_EMAIL = "operator@nova.test";
    protected static final String OPERATOR_PASS  = "operator123";
    protected static final String MATH_EMAIL     = "math@nova.test";
    protected static final String MATH_PASS      = "math123";
    protected static final String PLAYER_EMAIL   = "player1@nova.test";
    protected static final String PLAYER_PASS    = "player123";

    /** Inicia sesión con un usuario semilla y devuelve su JWT. */
    protected String loginAndGetToken(String email, String password) throws Exception {
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
