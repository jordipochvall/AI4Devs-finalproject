package com.novacasino.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.assertj.core.api.Assertions.assertThat;

/** HU-37 — the simulation executor must be explicitly bounded, not Spring's unbounded default. */
class EngineConfigTest {

    @Test
    void simulationExecutorIsBoundedToTheConfiguredSizes() {
        final EngineConfig config = new EngineConfig();
        final ThreadPoolTaskExecutor executor = config.simulationTaskExecutor(2, 4, 10);

        assertThat(executor.getCorePoolSize()).isEqualTo(2);
        assertThat(executor.getMaxPoolSize()).isEqualTo(4);
        assertThat(executor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(10);
    }
}
