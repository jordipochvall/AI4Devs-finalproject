package com.novacasino.api.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novacasino.api.player.GameConfigMapper;
import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.simulator.SimulationResult;
import com.novacasino.simulator.SimulationRunner;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-31 · guard de regresión — simula las <strong>configuraciones semilla reales</strong> a través del
 * mismo pipeline que producción (mapper → compiler → simulador) y verifica que el RTP empírico queda
 * dentro de una banda del RTP objetivo declarado. Evita reintroducir una config descalibrada (el bug
 * del ~3000%). También imprime las métricas para poder recalibrar.
 */
class SeedConfigRtpTest {

    private static final long SPINS = 1_000_000L;
    private static final long BET_CENTS = 100L;         // múltiplo de 5 y 10 líneas
    private static final long SEED = 20_260_702L;
    private static final double BAND = 0.05;            // ±5 pp respecto al target declarado
    private static final RngFactory RNG = seed -> new java.util.Random(seed)::nextInt;

    private final GameConfigMapper mapper = new GameConfigMapper();
    private final GameCompiler compiler = new GameCompiler();
    private final ObjectMapper json = new ObjectMapper();

    private record SeedGame(String name, String config, double target) { }

    @Test
    void seedConfigsRtpWithinBandOfTarget() throws Exception {
        // Same source of truth as production seeding (SeedDataLoader reads these resources).
        final List<SeedGame> games = List.of(
                new SeedGame("egyptian", SeedDataLoader.readConfig("egyptian"), 0.9500),
                new SeedGame("fruits",   SeedDataLoader.readConfig("fruits"),   0.9200),
                new SeedGame("space",    SeedDataLoader.readConfig("space"),    0.9650));

        final List<String> outOfBand = new ArrayList<>();
        for (int i = 0; i < games.size(); i++) {
            final SeedGame g = games.get(i);
            // Distinct configId per game: the compiler caches by id (same id → same compiled game).
            final CompiledGame game = compiler.compile(i + 1L, mapper.toSpec(json.readTree(g.config())));
            final SimulationResult r = new SimulationRunner(RNG).run(game, SPINS, BET_CENTS, SEED);
            System.out.printf(
                    "[RTP] %-9s target=%.2f%%  rtp=%7.2f%%  base=%7.2f%%  free=%6.2f%%  hit=%.2f%%  maxWin=%.1fx%n",
                    g.name(), g.target() * 100, r.rtp() * 100, r.rtpBaseGame() * 100, r.rtpFreeSpins() * 100,
                    r.hitFrequency() * 100, r.maxWinMultiplier());
            if (Math.abs(r.rtp() - g.target()) > BAND) {
                outOfBand.add(String.format("%s: rtp=%.4f, target=%.4f (|Δ|=%.4f > %.2f)",
                        g.name(), r.rtp(), g.target(), Math.abs(r.rtp() - g.target()), BAND));
            }
        }

        assertThat(outOfBand)
                .as("Seed configs whose empirical RTP is out of band (recalibrate their math): %s", outOfBand)
                .isEmpty();
    }
}
