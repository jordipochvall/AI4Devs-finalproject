package com.novacasino.simulator;

import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.GameCompiler;
import com.novacasino.domain.engine.GameConfigSpec;
import com.novacasino.domain.engine.GameConfigSpec.FreeSpinsSpec;
import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.engine.GameConfigSpec.WildSpec;
import com.novacasino.domain.engine.SymbolKind;
import com.novacasino.domain.rng.RngFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * HU-2-QA-01 · AC3 / HU-2-DEV-01 — performance + functional-regression guard. Runs 10,000,000 spins
 * of a representative 5x3 free-spins game and checks two things:
 * <ol>
 *   <li><b>Time budget</b>: it finishes well under the 10-minute SLA (zero-allocation kernel +
 *       parallel workers, readme §2.1.7).</li>
 *   <li><b>Functional regression</b>: the aggregated metrics (RTP, hit frequency, volatility) match
 *       a versioned baseline bit-for-bit. The run uses a <b>fixed parallelism and seed</b>, so the
 *       result is reproducible on any machine regardless of its core count; a bug that mis-accounts
 *       prizes shifts the metrics and fails the build.</li>
 * </ol>
 *
 * <p>Tagged {@code perf}: excluded from the normal build, run by the dedicated CI job with
 * {@code -Dgroups=perf -DexcludedGroups=}. Writes {@code SimulationResult.json} and
 * {@code duration_ms.txt} to {@code target/perf/} for the workflow to publish as artifacts.
 * Re-baseline with {@code -Dperf.generate=true} and review the diff.
 */
@Tag("perf")
class SimulationPerfTest {

    private static final long SLA_MS = 10 * 60 * 1000L; // 10 minutes
    private static final long SPINS = 10_000_000L;
    private static final long BET = 100L;
    private static final long SEED = 1L;
    private static final int PARALLELISM = 4;           // fixed → machine-independent result
    private static final double TOLERANCE = 1e-6;
    private static final RngFactory RNG = seed -> new Random(seed)::nextInt;

    private static final String BASELINE_RESOURCE = "/perf/baseline.properties";
    private static final Path BASELINE_FILE = Path.of("src", "test", "resources", "perf", "baseline.properties");
    private static final Path ARTIFACT_DIR = Path.of("target", "perf");

    @Test
    void tenMillionSpinsUnderSlaAndNoMetricRegression() {
        final CompiledGame game = new GameCompiler().compile(1L, perfConfig());
        final SimulationResult r = new SimulationRunner(RNG, PARALLELISM).run(game, SPINS, BET, SEED);

        writeArtifacts(r);

        assertThat(r.numSpins()).isEqualTo(SPINS);
        assertThat(r.durationMs())
                .as("10M spins took %d ms (SLA %d ms)", r.durationMs(), SLA_MS)
                .isLessThan(SLA_MS);

        if (Boolean.getBoolean("perf.generate")) {
            writeBaseline(r);
            return;
        }
        final Properties baseline = loadBaseline();
        assertThat(r.rtp()).as("RTP regression")
                .isCloseTo(Double.parseDouble(baseline.getProperty("rtp")), within(TOLERANCE));
        assertThat(r.hitFrequency()).as("hit-frequency regression")
                .isCloseTo(Double.parseDouble(baseline.getProperty("hitFrequency")), within(TOLERANCE));
        assertThat(r.volatility()).as("volatility regression")
                .isCloseTo(Double.parseDouble(baseline.getProperty("volatility")), within(TOLERANCE));
    }

    // -------------------------------------------------------------------------

    private Properties loadBaseline() {
        try (InputStream in = SimulationPerfTest.class.getResourceAsStream(BASELINE_RESOURCE)) {
            if (in == null) {
                throw new AssertionError("Perf baseline missing; generate it with -Dperf.generate=true");
            }
            final Properties props = new Properties();
            props.load(in);
            return props;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeBaseline(final SimulationResult r) {
        try {
            Files.createDirectories(BASELINE_FILE.getParent());
            final String content = "# Perf baseline (fixed seed/parallelism) — regenerate with -Dperf.generate=true\n"
                    + "rtp=" + r.rtp() + "\n"
                    + "hitFrequency=" + r.hitFrequency() + "\n"
                    + "volatility=" + r.volatility() + "\n";
            Files.writeString(BASELINE_FILE, content, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Publishes the headline metrics and the duration as CI artifacts (AC4). */
    private void writeArtifacts(final SimulationResult r) {
        try {
            Files.createDirectories(ARTIFACT_DIR);
            final String json = "{"
                    + "\"numSpins\":" + r.numSpins()
                    + ",\"betCents\":" + r.betCents()
                    + ",\"durationMs\":" + r.durationMs()
                    + ",\"rtp\":" + r.rtp()
                    + ",\"rtpStdError\":" + r.rtpStdError()
                    + ",\"rtpBaseGame\":" + r.rtpBaseGame()
                    + ",\"rtpFreeSpins\":" + r.rtpFreeSpins()
                    + ",\"hitFrequency\":" + r.hitFrequency()
                    + ",\"volatility\":" + r.volatility()
                    + ",\"maxWinMultiplier\":" + r.maxWinMultiplier()
                    + ",\"freeSpinTriggerFrequency\":" + r.freeSpinTriggerFrequency()
                    + ",\"longestDryStreak\":" + r.longestDryStreak()
                    + "}";
            Files.writeString(ARTIFACT_DIR.resolve("SimulationResult.json"), json, StandardCharsets.UTF_8);
            Files.writeString(ARTIFACT_DIR.resolve("duration_ms.txt"),
                    Long.toString(r.durationMs()), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 5x3, 5 paylines, wild + bounded free spins — representative load (no divergent retrigger). */
    private static GameConfigSpec perfConfig() {
        final List<SymbolSpec> symbols = List.of(
                new SymbolSpec("W", SymbolKind.WILD),
                new SymbolSpec("S", SymbolKind.SCATTER),
                new SymbolSpec("A", SymbolKind.REGULAR),
                new SymbolSpec("B", SymbolKind.REGULAR),
                new SymbolSpec("C", SymbolKind.REGULAR));
        final List<List<String>> reels = List.of(
                List.of("A", "B", "C", "W", "A", "S", "B", "C", "A", "B", "C", "A"),
                List.of("B", "C", "A", "B", "W", "C", "A", "S", "B", "C", "A", "B"),
                List.of("C", "A", "B", "S", "C", "W", "A", "B", "C", "A", "B", "C"),
                List.of("A", "C", "B", "A", "W", "C", "S", "B", "A", "C", "B", "A"),
                List.of("C", "B", "A", "C", "A", "B", "W", "C", "S", "A", "B", "C"));
        final List<List<Integer>> paylines = List.of(
                List.of(1, 1, 1, 1, 1), List.of(0, 0, 0, 0, 0), List.of(2, 2, 2, 2, 2),
                List.of(0, 1, 2, 1, 0), List.of(2, 1, 0, 1, 2));
        final List<PaytableEntry> paytable = List.of(
                new PaytableEntry("A", Map.of(3, 5L, 4, 20L, 5, 100L)),
                new PaytableEntry("B", Map.of(3, 3L, 4, 12L, 5, 60L)),
                new PaytableEntry("C", Map.of(3, 2L, 4, 8L, 5, 40L)));
        final Map<String, Map<Integer, Long>> scatterPays =
                Map.of("S", Map.of(3, 5L, 4, 20L, 5, 100L));
        final FreeSpinsSpec freeSpins =
                new FreeSpinsSpec("S", 3, Map.of(3, 8, 4, 12, 5, 20), 2, false);
        return new GameConfigSpec(5, 3, symbols, reels, paylines, paytable, scatterPays,
                new WildSpec(Set.of(SymbolKind.REGULAR)), freeSpins);
    }
}
