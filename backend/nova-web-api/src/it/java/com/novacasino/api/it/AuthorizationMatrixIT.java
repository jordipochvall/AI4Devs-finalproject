package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AC2: role × surface authorization matrix.
 * Each role only accesses its own group of routes; another role → 403; no token → 401.
 *
 * <p>Surface routes may not have controllers yet (added in HU-5..HU-8); that is why the
 * correct role "passes" security and gets a status other than 401/403 (404 today). The key
 * assertions of this ticket are the 401/403 ones.
 */
class AuthorizationMatrixIT extends AbstractIntegrationTest {

    private static final String PLAYER_PATH   = "/api/v1/player/games";
    private static final String OPERATOR_PATH = "/api/v1/operator/players";
    private static final String MATH_PATH     = "/api/v1/math/games";

    // --- AC: no token → 401 on every protected surface ---

    @Test
    void noToken_protectedSurfaces_return401() throws Exception {
        mockMvc.perform(get(PLAYER_PATH)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(OPERATOR_PATH)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(MATH_PATH)).andExpect(status().isUnauthorized());
    }

    // --- PLAYER ---

    @Test
    void playerToken_matrix() throws Exception {
        String token = loginAndGetToken(PLAYER_EMAIL, PLAYER_PASS);
        expectForbidden(OPERATOR_PATH, token);
        expectForbidden(MATH_PATH, token);
        expectAllowed(PLAYER_PATH, token);   // its own surface
    }

    // --- OPERATOR ---

    @Test
    void operatorToken_matrix() throws Exception {
        String token = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        expectForbidden(PLAYER_PATH, token);
        expectForbidden(MATH_PATH, token);
        expectAllowed(OPERATOR_PATH, token);
    }

    // --- MATH_ANALYST ---

    @Test
    void mathToken_matrix() throws Exception {
        String token = loginAndGetToken(MATH_EMAIL, MATH_PASS);
        expectForbidden(PLAYER_PATH, token);
        expectForbidden(OPERATOR_PATH, token);
        expectAllowed(MATH_PATH, token);
    }

    // -------------------------------------------------------------------------

    private void expectForbidden(String path, String token) throws Exception {
        mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /** The correct role passes security: a status other than 401/403. */
    private void expectAllowed(String path, String token) throws Exception {
        int sc = mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
        assertThat(sc).as("acceso permitido a %s", path).isNotIn(401, 403);
    }
}
