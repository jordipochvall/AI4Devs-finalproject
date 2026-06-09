package com.novacasino.simulator;

import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.engine.GameConfigSpec;
import com.novacasino.domain.engine.GameConfigSpec.FreeSpinsSpec;
import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.engine.SymbolKind;
import com.novacasino.domain.rng.RngFactory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * HU-2-BE-01 — the mass simulator. Verifies the aggregated metrics over a config whose RTP is known
 * by construction (AC1/AC1b), parallel aggregation correctness (AC4), the free-spins decomposition
 * and the bet/spin validation.
 */
class SimulationRunnerTest {

    /** java.util.Random-backed factory (same family as production, reproducible). */
    private final RngFactory rngFactory = seed -> new Random(seed)::nextInt;
    private final GameCompiler compiler = new GameCompiler();

    @Test
    void aggregatesMetricsAndConvergesToTheConstructedRtp() {
        // 3x3, single payline, strip = 5×A + 5×B; only 3-of-a-kind pays.
        // RTP = pA³·mA + pB³·mB = .5³·4 + .5³·2 = .75
        final CompiledGame game = compiler.compile(1L, knownRtpGame(5, 4, 2));
        final SimulationRunner runner = new SimulationRunner(rngFactory, 4);

        final SimulationResult r = runner.run(game, 200_000L, 100L, 42L);

        assertThat(r.numSpins()).isEqualTo(200_000L);       // LongAdder aggregation, no lost rounds (AC4)
        assertThat(r.rtp()).isCloseTo(0.75, within(0.03));  // converges to the constructed RTP
        assertThat(r.rtpStdError()).isGreaterThan(0.0);
        assertThat(r.volatility()).isGreaterThan(0.0);
        assertThat(r.hitFrequency()).isBetween(0.0, 1.0);
        assertThat(r.maxWinMultiplier()).isGreaterThanOrEqualTo(0.0);
        assertThat(r.durationMs()).isGreaterThanOrEqualTo(0L);

        // Decomposition: base + free spins == total (no free spins here → all base).
        assertThat(r.rtpBaseGame() + r.rtpFreeSpins()).isCloseTo(r.rtp(), within(1e-9));
        assertThat(r.rtpFreeSpins()).isZero();

        // Histogram covers every round.
        assertThat(r.prizeDistribution().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(200_000L);
        // Convergence curve sampled and reel-strip frequencies computed.
        assertThat(r.convergenceSample()).isNotEmpty();
        assertThat(r.reelSymbolFrequencies()).hasSize(3);
        assertThat(r.reelSymbolFrequencies().get(0)).containsEntry("A", 0.5).containsEntry("B", 0.5);
        // Per-symbol RTP contribution present.
        assertThat(r.rtpBreakdown().bySymbol()).containsKey("A");
    }

    @Test
    void sameSeedAndParallelismProduceIdenticalResults() {
        final CompiledGame game = compiler.compile(2L, knownRtpGame(5, 4, 2));
        final SimulationRunner runner = new SimulationRunner(rngFactory, 4);

        final SimulationResult a = runner.run(game, 50_000L, 100L, 7L);
        final SimulationResult b = runner.run(game, 50_000L, 100L, 7L);

        assertThat(b.rtp()).isEqualTo(a.rtp());
        assertThat(b.hitFrequency()).isEqualTo(a.hitFrequency());
        assertThat(b.maxWinMultiplier()).isEqualTo(a.maxWinMultiplier());
        assertThat(b.longestDryStreak()).isEqualTo(a.longestDryStreak());
    }

    @Test
    void measuresFreeSpinsContributionAndTriggerFrequency() {
        final CompiledGame game = compiler.compile(3L, freeSpinsGame());
        final SimulationRunner runner = new SimulationRunner(rngFactory, 4);

        final SimulationResult r = runner.run(game, 100_000L, 100L, 99L);

        assertThat(r.freeSpinTriggerFrequency()).isGreaterThan(0.0); // free spins do trigger
        assertThat(r.rtpFreeSpins()).isGreaterThan(0.0);             // and contribute prizes
        assertThat(r.rtpBaseGame() + r.rtpFreeSpins()).isCloseTo(r.rtp(), within(1e-9));
    }

    @Test
    void rejectsInvalidArguments() {
        final CompiledGame game = compiler.compile(4L, knownRtpGame(5, 4, 2));
        final SimulationRunner runner = new SimulationRunner(rngFactory, 2);

        assertThatThrownBy(() -> runner.run(game, 0L, 100L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        // 3 paylines? no — this game has 1 payline, so make it fail the positive check instead.
        assertThatThrownBy(() -> runner.run(game, 1000L, 0L, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------------------------------------------------------

    /** 3x3, single payline, strip = k×A + (10-k)×B; A and B pay only a 3-of-a-kind. */
    private static GameConfigSpec knownRtpGame(final int aCount, final int mA, final int mB) {
        final java.util.List<String> strip = new java.util.ArrayList<>();
        for (int i = 0; i < aCount; i++) {
            strip.add("A");
        }
        for (int i = aCount; i < 10; i++) {
            strip.add("B");
        }
        return new GameConfigSpec(3, 3,
                List.of(new SymbolSpec("A", SymbolKind.REGULAR), new SymbolSpec("B", SymbolKind.REGULAR)),
                List.of(strip, strip, strip),
                List.of(List.of(1, 1, 1)),
                List.of(new PaytableEntry("A", Map.of(3, (long) mA)),
                        new PaytableEntry("B", Map.of(3, (long) mB))),
                Map.of(), null, null);
    }

    /** 3x3 single-line game with a scatter-heavy strip so free spins trigger often. */
    private static GameConfigSpec freeSpinsGame() {
        final List<String> strip = List.of("S", "S", "A", "S", "A", "S", "A", "S", "A", "A");
        return new GameConfigSpec(3, 3,
                List.of(new SymbolSpec("A", SymbolKind.REGULAR), new SymbolSpec("S", SymbolKind.SCATTER)),
                List.of(strip, strip, strip),
                List.of(List.of(1, 1, 1)),
                List.of(new PaytableEntry("A", Map.of(3, 5L))),
                Map.of(),
                null,
                new FreeSpinsSpec("S", 3, Map.of(3, 2, 4, 3, 5, 4, 6, 5, 7, 6, 8, 7, 9, 8), 2, false));
    }
}
