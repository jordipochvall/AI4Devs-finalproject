package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AC2: matriz de autorización rol × superficie.
 * Cada rol solo accede a su grupo de rutas; otro rol → 403; sin token → 401.
 *
 * <p>Las rutas de superficie aún no tienen controladores (se añaden en HU-5..HU-8);
 * por eso el rol correcto "pasa" la seguridad y obtiene un estado distinto de 401/403
 * (hoy 404). Las aserciones clave de este ticket son los 401/403.
 */
class AuthorizationMatrixIT extends AbstractIntegrationTest {

    private static final String PLAYER_PATH   = "/api/v1/player/games";
    private static final String OPERATOR_PATH = "/api/v1/operator/players";
    private static final String MATH_PATH     = "/api/v1/math/games";

    // --- AC: sin token → 401 en todas las superficies protegidas ---

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
        expectAllowed(PLAYER_PATH, token);   // su propia superficie
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

    /** El rol correcto supera la seguridad: estado distinto de 401/403. */
    private void expectAllowed(String path, String token) throws Exception {
        int sc = mockMvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getStatus();
        assertThat(sc).as("acceso permitido a %s", path).isNotIn(401, 403);
    }
}
