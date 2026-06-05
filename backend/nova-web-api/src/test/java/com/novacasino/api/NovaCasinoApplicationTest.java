package com.novacasino.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test del esqueleto. El test de contexto Spring completo (con Testcontainers)
 * se añade en HU-1-QA-01 una vez que el esquema DB y los beans estén implementados.
 */
class NovaCasinoApplicationTest {

    @Test
    void applicationClassLoads() {
        assertNotNull(NovaCasinoApplication.class);
    }
}
