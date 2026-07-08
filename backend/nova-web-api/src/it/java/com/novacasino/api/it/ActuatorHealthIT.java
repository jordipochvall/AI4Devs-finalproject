package com.novacasino.api.it;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-33 · guard de regresión — el healthcheck público debe seguir siendo alcanzable sin token (lo
 * exige el healthcheck de {@code deploy/docker-compose.prod.yml} y {@code scripts/smoke-test.sh}),
 * y ningún otro endpoint protegido debe quedar abierto por error al permitir actuator.
 */
class ActuatorHealthIT extends AbstractIntegrationTest {

    @Test
    void healthIsReachableWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void protectedEndpointStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/player/games"))
                .andExpect(status().isUnauthorized());
    }
}
