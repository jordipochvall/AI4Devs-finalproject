package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AC2: API error messages are returned in the language of {@code Accept-Language}.
 * A login with invalid credentials (401) is used as a reproducible error.
 */
class I18nApiIT extends AbstractIntegrationTest {

    private static final String BAD_LOGIN = """
            {"email":"%s","password":"definitely-wrong"}""".formatted(PLAYER_EMAIL);

    @Test
    void error_withAcceptLanguageEn_returnsEnglish() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Accept-Language", "en")
                        .contentType(APPLICATION_JSON).content(BAD_LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid credentials."));
    }

    @Test
    void error_withAcceptLanguageEs_returnsSpanish() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .header("Accept-Language", "es")
                        .contentType(APPLICATION_JSON).content(BAD_LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales incorrectas."));
    }

    @Test
    void error_withoutAcceptLanguage_defaultsToSpanish() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(BAD_LOGIN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales incorrectas."));
    }
}
