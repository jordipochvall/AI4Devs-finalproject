package com.novacasino.api.config;

import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.simulator.SimulationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

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
}
