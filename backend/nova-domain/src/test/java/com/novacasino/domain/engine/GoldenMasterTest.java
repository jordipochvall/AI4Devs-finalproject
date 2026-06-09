package com.novacasino.domain.engine;

import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.rng.RngEngine;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * HU-1-QA-01 · AC5/AC6 — golden master of the {@link SpinKernel}: a frozen corpus of
 * {@code (config, seed, bet) → result} fixtures (versioned in
 * {@code src/test/resources/golden/golden-master.properties}) that the engine must reproduce
 * <strong>bit by bit</strong>. Each fixture is condensed to a SHA-256 of the canonical sequence of
 * spins; if the engine's output drifts the hash changes and the build fails.
 *
 * <p>This is a tripwire, not a backward-compat contract: a failure forces an explicit decision —
 * revert an accidental change, or re-baseline intentionally by regenerating the corpus
 * ({@code mvn test -Dgolden.generate=true}, then review and commit the diff).
 */
class GoldenMasterTest {

    private static final String RESOURCE = "/golden/golden-master.properties";
    private static final Path SOURCE_FILE =
            Path.of("src", "test", "resources", "golden", "golden-master.properties");

    private final GameCompiler compiler = new GameCompiler();

    /** A fixture: a named, deterministic run of the kernel. */
    private record Fixture(String name, GameConfigSpec spec, long configId, long seed, long betCents) {
    }

    private List<Fixture> fixtures() {
        final GameConfigSpec egyptian = EngineTestSupport.egyptianSpecBounded();
        return List.of(
                new Fixture("egyptian-bounded-seed-1", egyptian, 1001L, 1L, 500L),
                new Fixture("egyptian-bounded-seed-7", egyptian, 1001L, 7L, 500L),
                new Fixture("egyptian-bounded-seed-42", egyptian, 1001L, 42L, 500L),
                new Fixture("ab-3x3-seed-5", abGame(), 2002L, 5L, 100L));
    }

    @Test
    void engineReproducesTheGoldenCorpusBitForBit() {
        final Map<String, String> actual = new LinkedHashMap<>();
        for (final Fixture fixture : fixtures()) {
            actual.put(fixture.name(), hashOf(fixture));
        }

        if (Boolean.getBoolean("golden.generate")) {
            writeCorpus(actual);
            return; // regeneration run: baseline written, nothing to assert
        }

        final Properties expected = loadCorpus();
        assertThat(expected.stringPropertyNames())
                .as("golden corpus must cover exactly the current fixtures")
                .containsExactlyInAnyOrderElementsOf(actual.keySet());
        actual.forEach((name, hash) ->
                assertThat(hash)
                        .as("golden master drift for fixture '%s' — the engine output changed", name)
                        .isEqualTo(expected.getProperty(name)));
    }

    // -------------------------------------------------------------------------

    /** Runs one fixture and returns the SHA-256 of its canonical spin sequence. */
    private String hashOf(final Fixture fixture) {
        final CompiledGame game = compiler.compile(fixture.configId(), fixture.spec());
        final SpinKernel kernel = new SpinKernel(game);
        final RngEngine rng = new Random(fixture.seed())::nextInt;
        final CanonicalSink sink = new CanonicalSink();
        kernel.spin(fixture.betCents(), rng, sink);
        return sha256(sink.canonical());
    }

    private Properties loadCorpus() {
        try (InputStream in = GoldenMasterTest.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                return fail("Golden corpus %s is missing. Generate it with -Dgolden.generate=true.", RESOURCE);
            }
            final Properties props = new Properties();
            props.load(in);
            return props;
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeCorpus(final Map<String, String> corpus) {
        try {
            Files.createDirectories(SOURCE_FILE.getParent());
            final StringBuilder sb = new StringBuilder("# Golden master of the SpinKernel — regenerate with -Dgolden.generate=true\n");
            corpus.forEach((name, hash) -> sb.append(name).append('=').append(hash).append('\n'));
            Files.writeString(SOURCE_FILE, sb.toString(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String sha256(final String input) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            final StringBuilder sb = new StringBuilder(64);
            for (final byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** A 3x3 single-line game with a mixed A/B strip (some wins, some not) for a varied sequence. */
    private static GameConfigSpec abGame() {
        final List<String> strip = List.of("A", "B", "A", "B", "A", "B", "A", "A", "B", "A");
        return new GameConfigSpec(3, 3,
                List.of(new SymbolSpec("A", SymbolKind.REGULAR), new SymbolSpec("B", SymbolKind.REGULAR)),
                List.of(strip, strip, strip),
                List.of(List.of(1, 1, 1)),
                List.of(new PaytableEntry("A", Map.of(3, 5L)), new PaytableEntry("B", Map.of(3, 2L))),
                Map.of(), null, null);
    }

    /** Sink that appends a canonical, stable line per spin (the bit-for-bit signature). */
    private static final class CanonicalSink implements RoundSink {
        private final StringBuilder sb = new StringBuilder();

        @Override
        public void onSpin(final int[] window, final int cols, final int rows,
                           final int[] winningLines, final int[] winningLineSymbols,
                           final int[] winningLineCounts, final long[] winningLineWins,
                           final int winningLineCount, final long winCents, final boolean freeSpin,
                           final int multiplier, final int scatterCount) {
            sb.append(freeSpin ? 'F' : 'B').append('|')
                    .append(multiplier).append('|')
                    .append(scatterCount).append('|')
                    .append(winCents).append('|');
            for (int i = 0; i < winningLineCount; i++) {
                sb.append(winningLines[i]).append(':').append(winningLineWins[i]).append(',');
            }
            sb.append('|');
            for (int i = 0; i < cols * rows; i++) {
                sb.append(window[i]).append(',');
            }
            sb.append('\n');
        }

        String canonical() {
            return sb.toString();
        }
    }
}
