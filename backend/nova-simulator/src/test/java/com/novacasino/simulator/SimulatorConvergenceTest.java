package com.novacasino.simulator;

import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.simulator.SimulationResult.ConvergencePoint;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * HU-2-QA-01 · AC2/AC5 — convergence test: over a config with a known RTP, the empirical RTP after 1M
 * spins falls within the confidence interval of the true value, and the {@code convergenceSample}
 * gets monotonically more stable (the running estimate is, on average, closer to the final RTP late
 * in the run than early on). A prize-accounting bug would push the RTP out of the CI and fail.
 */
class SimulatorConvergenceTest {

    private static final RngFactory RNG = seed -> new Random(seed)::nextInt;

    @Test
    void rtpConvergesWithinConfidenceIntervalAndStabilises() {
        // pA=0.5, mA=4, mB=2 → expected RTP = .5³·4 + .5³·2 = 0.75
        final CompiledGame game = new GameCompiler()
                .compile(1L, SimulatorRtpPropertyTest.syntheticGame(5, 4, 2));
        final SimulationResult r = new SimulationRunner(RNG, 4).run(game, 1_000_000L, 100L, 2026L);

        final double expected = 0.75;
        final double tolerance = 5.0 * r.rtpStdError() + 1e-6;
        assertThat(Math.abs(r.rtp() - expected))
                .as("1M-spin RTP %.5f within CI of %.2f", r.rtp(), expected)
                .isLessThanOrEqualTo(tolerance);

        // The convergence curve stabilises: the late third deviates from the final RTP less than the
        // early third (the running estimate's error shrinks as N grows).
        final List<ConvergencePoint> points = r.convergenceSample();
        assertThat(points.size()).isGreaterThanOrEqualTo(6);
        final double rtp = r.rtp();
        final int third = points.size() / 3;
        final double earlyError = avgDeviation(points.subList(0, third), rtp);
        final double lateError = avgDeviation(points.subList(points.size() - third, points.size()), rtp);
        assertThat(lateError).isLessThan(earlyError);
    }

    private static double avgDeviation(final List<ConvergencePoint> points, final double target) {
        return points.stream().mapToDouble(p -> Math.abs(p.rtp() - target)).average().orElse(0);
    }
}
