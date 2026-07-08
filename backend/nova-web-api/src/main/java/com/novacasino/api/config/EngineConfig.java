package com.novacasino.api.config;

import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.simulator.SimulationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Wires the pure-domain engine pieces as Spring beans. The {@link GameCompiler} is a singleton so its
 * per-{@code configId} cache is shared across all spins (readme §2.1.7). {@code @EnableAsync} backs
 * the asynchronous simulation execution (HU-2-BE-02).
 */
@Configuration
@EnableAsync
public class EngineConfig {

    /** Single, shared compiler/cache for the whole application. */
    @Bean
    public GameCompiler gameCompiler() {
        return new GameCompiler();
    }

    /** Mass simulator; uses the configured {@link RngFactory} adapter. */
    @Bean
    public SimulationRunner simulationRunner(final RngFactory rngFactory) {
        return new SimulationRunner(rngFactory);
    }

    /**
     * Bounded executor for {@link com.novacasino.api.math.SimulationExecutor} (HU-37): a demo VPS has
     * limited CPU, and each simulation already fans out across cores internally, so the number of
     * *concurrent* async workers must stay small and explicitly named rather than falling back to
     * Spring's unbounded default. The business-level cap in {@code SimulationUseCase} rejects launches
     * before they reach this executor; this bound is a second, infrastructure-level safety net.
     */
    @Bean("simulationTaskExecutor")
    public ThreadPoolTaskExecutor simulationTaskExecutor(
            @Value("${app.simulation.executor.core-pool-size:2}") final int corePoolSize,
            @Value("${app.simulation.executor.max-pool-size:4}") final int maxPoolSize,
            @Value("${app.simulation.executor.queue-capacity:10}") final int queueCapacity) {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("sim-exec-");
        executor.initialize();
        return executor;
    }
}
