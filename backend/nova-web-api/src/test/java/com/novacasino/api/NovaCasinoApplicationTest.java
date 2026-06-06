package com.novacasino.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Skeleton smoke test. The full Spring context test (with Testcontainers) is added in
 * HU-1-QA-01 once the DB schema and beans are implemented.
 */
class NovaCasinoApplicationTest {

    @Test
    void applicationClassLoads() {
        assertNotNull(NovaCasinoApplication.class);
    }
}
