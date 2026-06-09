package com.novacasino.simulator;

import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.engine.GameConfigSpec;
import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.engine.SymbolKind;
import com.novacasino.domain.rng.RngFactory;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-2-QA-01 · AC2/AC5 — through the full {@link SimulationRunner}, for synthetic configs whose RTP
 * is known by construction ({@code pA³·mA + pB³·mB}), the empirical RTP after many spins lands within
 * the <strong>confidence interval</strong> of the true value. The tolerance scales with the reported
 * standard error (not a fixed threshold), so it stays stable in high-volatility configs; a prize bug
 * that shifts the RTP would exceed several standard errors and fail.
 */
class SimulatorRtpPropertyTest {

    private static final long SPINS = 300_000L;
    private static final long BET = 100L;          // single payline → lineBet == bet
    private static final double Z = 5.0;           // generous CI half-width (≈5σ) to avoid flakiness
    private static final RngFactory RNG = seed -> new Random(seed)::nextInt;

    private final GameCompiler compiler = new GameCompiler();

    @Property(tries = 12, seed = "20260607")
    void empiricalRtpWithinConfidenceInterval(
            @ForAll @IntRange(min = 3, max = 7) final int aCount,
            @ForAll @IntRange(min = 2, max = 6) final int mA,
            @ForAll @IntRange(min = 2, max = 6) final int mB) {

        final double pA = aCount / 10.0;
        final double pB = 1.0 - pA;
        final double expected = pA * pA * pA * mA + pB * pB * pB * mB;

        final CompiledGame game = compiler.compile(System.nanoTime(), syntheticGame(aCount, mA, mB));
        final SimulationResult r = new SimulationRunner(RNG, 4).run(game, SPINS, BET, 1234L);

        final double tolerance = Z * r.rtpStdError() + 1e-6;
        assertThat(Math.abs(r.rtp() - expected))
                .as("empirical RTP %.5f vs expected %.5f (±%.5f)", r.rtp(), expected, tolerance)
                .isLessThanOrEqualTo(tolerance);
    }

    /** 3x3, single payline, strip = k×A + (10-k)×B; only a 3-of-a-kind pays. */
    static GameConfigSpec syntheticGame(final int aCount, final int mA, final int mB) {
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
}
