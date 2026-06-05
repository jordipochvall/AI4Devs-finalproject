package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AC1: cobertura de todos los códigos del flujo de auth (201/200/401/409/422)
 * contra un Postgres real con el esquema y el seed aplicados.
 */
class AuthFlowIT extends AbstractIntegrationTest {

    private String uniqueEmail() {
        return "it-" + UUID.randomUUID() + "@test.com";
    }

    // --- 201: registro válido devuelve JWT y NO expone la password (AC4) ---

    @Test
    void register_valid_returns201WithToken() throws Exception {
        String email = uniqueEmail();
        String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"1990-05-20","locale":"es"}"""
                .formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value(email))
                .andExpect(jsonPath("$.user.role").value("PLAYER"))
                // AC4: la respuesta no contiene password ni hash
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    // --- 422: menor de edad ---

    @Test
    void register_underAge_returns422() throws Exception {
        String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"%s","locale":"es"}"""
                .formatted(uniqueEmail(), LocalDate.now().minusYears(15));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    // --- 409: email duplicado ---

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        String email = uniqueEmail();
        String body = """
                {"email":"%s","password":"Sup3rSecret!","birthDate":"1990-05-20","locale":"es"}"""
                .formatted(email);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        // segundo registro con el mismo email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    // --- 200: login con credenciales semilla ---

    @Test
    void login_validSeedUser_returns200WithToken() throws Exception {
        String body = """
                {"email":"%s","password":"%s"}""".formatted(PLAYER_EMAIL, PLAYER_PASS);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("PLAYER"));
    }

    // --- 401: credenciales inválidas ---

    @Test
    void login_wrongPassword_returns401() throws Exception {
        String body = """
                {"email":"%s","password":"wrong-password"}""".formatted(PLAYER_EMAIL);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    // --- 400: payload mal formado / validación de formato ---

    @Test
    void register_invalidEmail_returns400() throws Exception {
        String body = """
                {"email":"not-an-email","password":"short","birthDate":"1990-05-20"}""";

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
