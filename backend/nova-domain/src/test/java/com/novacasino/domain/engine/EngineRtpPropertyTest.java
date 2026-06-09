package com.novacasino.domain.engine;

import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.rng.RngEngine;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-1-QA-01 · AC2 — property-based check of the engine over <strong>synthetic configs whose RTP is
 * known by construction</strong> (not real games, whose target RTP the mathematician declares).
 *
 * <p>Each config is a 3x3 single-line game with two regular symbols A and B over a length-10 strip
 * with {@code k} A's. Only a full 3-of-a-kind pays, so the per-spin expected return (as a fraction of
 * the bet, with a single payline so {@code lineBet == betCents}) is closed-form:
 * {@code RTP = pA³·mA + pB³·mB} with {@code pA = k/10}. The empirical RTP after many spins must
 * converge to it within a tolerance comfortably above the standard error.
 */
class EngineRtpPropertyTest {

    private static final int SPINS = 300_000;
    private static final long BET_CENTS = 100L;          // single payline → lineBet == betCents
    private static final long RNG_SEED = 987_654_321L;   // fixed → deterministic, non-flaky
    private static final double TOLERANCE = 0.05;        // ≫ standard error for these bounded payouts

    private final GameCompiler compiler = new GameCompiler();

    @Property(tries = 15, seed = "42")
    void empiricalRtpConvergesToTheConstructedRtp(
            @ForAll @IntRange(min = 3, max = 7) final int aCount,
            @ForAll @IntRange(min = 2, max = 6) final int mA,
            @ForAll @IntRange(min = 2, max = 6) final int mB) {

        final double pA = aCount / 10.0;
        final double pB = 1.0 - pA;
        final double expectedRtp = pA * pA * pA * mA + pB * pB * pB * mB;

        final GameConfigSpec spec = syntheticGame(aCount, mA, mB);
        final CompiledGame game = compiler.compile(System.nanoTime(), spec);
        final SpinKernel kernel = new SpinKernel(game);
        final RngEngine rng = new Random(RNG_SEED)::nextInt;
        final SummingSink sink = new SummingSink();

        for (int i = 0; i < SPINS; i++) {
            kernel.spin(BET_CENTS, rng, sink);
        }

        final double empiricalRtp = sink.totalWin / (double) (SPINS * BET_CENTS);
        assertThat(empiricalRtp).isCloseTo(expectedRtp, org.assertj.core.data.Offset.offset(TOLERANCE));
    }

    /** 3x3, single middle payline, strip = k×A + (10-k)×B; only 3-of-a-kind pays. */
    private static GameConfigSpec syntheticGame(final int aCount, final int mA, final int mB) {
        final List<String> strip = new ArrayList<>(10);
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

    /** Minimal sink: accumulates the total win in cents. */
    private static final class SummingSink implements RoundSink {
        private long totalWin;

        @Override
        public void onSpin(final int[] window, final int cols, final int rows,
                           final int[] winningLines, final int[] winningLineSymbols,
                           final int[] winningLineCounts, final long[] winningLineWins,
                           final int winningLineCount, final long winCents, final boolean freeSpin,
                           final int multiplier, final int scatterCount) {
            totalWin += winCents;
        }
    }
}
