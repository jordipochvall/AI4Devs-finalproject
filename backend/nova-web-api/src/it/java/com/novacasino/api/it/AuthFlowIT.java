package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AC1: coverage of all auth-flow status codes (201/200/401/409/422) against a real Postgres
 * with the schema and seed applied.
 */
class AuthFlowIT extends AbstractIntegrationTest {

    private String uniqueEmail() {
        return "it-" + UUID.randomUUID() + "@test.com";
    }

    // --- 201: valid registration returns a JWT and does NOT expose the password (AC4) ---

    @Test
    void register_valid_returns201WithToken() throws Exception {
        final String email = uniqueEmail();
        final String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"1990-05-20","locale":"es"}"""
                .formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("PLAYER"))
                // AC4: the response contains neither password nor hash
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    // --- 422: under age ---

    @Test
    void register_underAge_returns422() throws Exception {
        final String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"%s","locale":"es"}"""
                .formatted(uniqueEmail(), LocalDate.now().minusYears(15));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    // --- 409: duplicate email ---

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        final String email = uniqueEmail();
        final String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"1990-05-20","locale":"es"}"""
                .formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        // second registration with the same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    // --- 200: login with seed credentials ---

    @Test
    void login_validSeedUser_returns200WithToken() throws Exception {
        final String body = """
                {"email":"%s","password":"%s"}""".formatted(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("PLAYER"));
    }

    // --- 401: invalid credentials ---

    @Test
    void login_wrongPassword_returns401() throws Exception {
        final String body = """
                {"email":"%s","password":"wrong-password"}""".formatted(PLAYER_EMAIL);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    // --- 400: malformed payload / format validation ---

    @Test
    void register_invalidEmail_returns400() throws Exception {
        final String body = """
                {"email":"not-an-email","password":"short","birthDate":"1990-05-20"}""";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
