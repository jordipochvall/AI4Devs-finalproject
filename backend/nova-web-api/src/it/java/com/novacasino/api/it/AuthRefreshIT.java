package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-13: refresh-token flow against a real Postgres — silent renewal, rotation (single use),
 * revocation on logout and rejection of unknown/invalid tokens.
 */
class AuthRefreshIT extends AbstractIntegrationTest {

    /** Logs in a seed user and returns the parsed auth response (token + refreshToken). */
    private JsonNode loginResponse(final String email, final String password) throws Exception {
        final String body = """
                {"email":"%s","password":"%s"}""".formatted(email, password);
        final String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json);
    }

    private String refreshToken(final String email, final String password) throws Exception {
        return loginResponse(email, password).get("refreshToken").asText();
    }

    // --- AC1: a valid refresh token renews the session with brand-new tokens ---

    @Test
    void refresh_validToken_returnsNewAccessAndRotatedRefresh() throws Exception {
        final JsonNode login = loginResponse(PLAYER_EMAIL, PLAYER_PASS);
        final String oldRefresh = login.get("refreshToken").asText();

        final String body = """
                {"refreshToken":"%s"}""".formatted(oldRefresh);
        final String json = mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("PLAYER"))
                .andReturn().getResponse().getContentAsString();

        // The refresh token is rotated: the new one differs from the presented one.
        final String newRefresh = objectMapper.readTree(json).get("refreshToken").asText();
        org.assertj.core.api.Assertions.assertThat(newRefresh).isNotEqualTo(oldRefresh);
    }

    // --- AC1/rotation: a rotated (already-consumed) refresh token cannot be reused ---

    @Test
    void refresh_reusedRotatedToken_returns401() throws Exception {
        final String oldRefresh = refreshToken(PLAYER_EMAIL, PLAYER_PASS);
        final String body = """
                {"refreshToken":"%s"}""".formatted(oldRefresh);

        // First use succeeds and revokes it.
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isOk());

        // Reusing the same (now revoked) token is rejected.
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    // --- AC3: logout revokes the refresh token ---

    @Test
    void logout_revokesRefreshToken() throws Exception {
        final String refresh = refreshToken(PLAYER_EMAIL, PLAYER_PASS);
        final String body = """
                {"refreshToken":"%s"}""".formatted(refresh);

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isNoContent());

        // After logout the token can no longer renew the session.
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    // --- AC2: an unknown/garbage refresh token is rejected ---

    @Test
    void refresh_unknownToken_returns401() throws Exception {
        final String body = """
                {"refreshToken":"this-token-does-not-exist"}""";

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    // --- 400: blank refresh token fails bean validation ---

    @Test
    void refresh_blankToken_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(APPLICATION_JSON).content("{\"refreshToken\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
