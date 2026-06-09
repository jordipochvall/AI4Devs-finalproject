package com.novacasino.domain.engine;

import com.novacasino.domain.engine.GameConfigSpec.FreeSpinsSpec;
import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.engine.GameConfigSpec.WildSpec;
import com.novacasino.domain.rng.RngEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Shared fixtures for the engine tests: deterministic {@link RngEngine} doubles, recording/no-op
 * {@link RoundSink}s and a couple of {@link GameConfigSpec} builders (a full 5x3 "Egyptian" game and
 * small fully-controlled games whose reels equal the visible window when every stop is {@code 0}).
 */
final class EngineTestSupport {

    private EngineTestSupport() {
    }

    // ------------------------------------------------------------------ RNG doubles

    /** RNG that replays a fixed script of stop positions (one per reel, in column order). */
    static final class ScriptedRng implements RngEngine {
        private final int[] script;
        private int pos;

        ScriptedRng(final int... script) {
            this.script = script;
        }

        @Override
        public int nextInt(final int boundExclusive) {
            return script[pos++] % boundExclusive;
        }
    }

    /** Allocation-free RNG that cycles through a fixed set of values forever. */
    static final class CyclingRng implements RngEngine {
        private final int[] values;
        private int pos;

        CyclingRng(final int... values) {
            this.values = values;
        }

        @Override
        public int nextInt(final int boundExclusive) {
            final int v = values[pos] % boundExclusive;
            pos = (pos + 1 == values.length) ? 0 : pos + 1;
            return v;
        }
    }

    /** Seedable PRNG used to assert determinism across two identical runs. */
    static final class SeededRng implements RngEngine {
        private final Random random;

        SeededRng(final long seed) {
            this.random = new Random(seed);
        }

        @Override
        public int nextInt(final int boundExclusive) {
            return random.nextInt(boundExclusive);
        }
    }

    // ------------------------------------------------------------------ sinks

    /** Immutable snapshot of one emitted spin. */
    record Spin(int[] window, int[] winningLines, long[] winningLineWins, long winCents,
                boolean freeSpin, int multiplier, int scatterCount) {
    }

    /** Sink that deep-copies every spin so the sequence can be inspected/compared. */
    static final class RecordingSink implements RoundSink {
        final List<Spin> spins = new ArrayList<>();

        @Override
        public void onSpin(final int[] window, final int cols, final int rows,
                           final int[] winningLines, final int[] winningLineSymbols,
                           final int[] winningLineCounts, final long[] winningLineWins,
                           final int winningLineCount, final long winCents, final boolean freeSpin,
                           final int multiplier, final int scatterCount) {
            spins.add(new Spin(
                    Arrays.copyOf(window, window.length),
                    Arrays.copyOf(winningLines, winningLineCount),
                    Arrays.copyOf(winningLineWins, winningLineCount),
                    winCents, freeSpin, multiplier, scatterCount));
        }
    }

    /** Sink that retains nothing — used by the allocation test. */
    static final class NoOpSink implements RoundSink {
        long lastWin;

        @Override
        public void onSpin(final int[] window, final int cols, final int rows,
                           final int[] winningLines, final int[] winningLineSymbols,
                           final int[] winningLineCounts, final long[] winningLineWins,
                           final int winningLineCount, final long winCents, final boolean freeSpin,
                           final int multiplier, final int scatterCount) {
            lastWin = winCents; // touch a field so the JIT cannot elide the call
        }
    }

    // ------------------------------------------------------------------ spec builders

    /** The 5x3 "Egyptian" game from readme §3.3.2. */
    static GameConfigSpec egyptianSpec() {
        final List<SymbolSpec> symbols = List.of(
                new SymbolSpec("WILD", SymbolKind.WILD),
                new SymbolSpec("SCATTER", SymbolKind.SCATTER),
                new SymbolSpec("ANUBIS", SymbolKind.REGULAR),
                new SymbolSpec("SCARAB", SymbolKind.REGULAR),
                new SymbolSpec("A", SymbolKind.REGULAR));
        final List<List<String>> reels = List.of(
                List.of("ANUBIS", "A", "SCARAB", "WILD", "A", "SCATTER", "SCARAB", "A"),
                List.of("A", "SCARAB", "ANUBIS", "A", "WILD", "SCARAB", "A", "SCATTER"),
                List.of("SCARAB", "A", "ANUBIS", "SCATTER", "A", "WILD", "SCARAB", "A"),
                List.of("A", "ANUBIS", "SCARAB", "A", "WILD", "A", "SCATTER", "SCARAB"),
                List.of("SCARAB", "A", "SCATTER", "ANUBIS", "A", "SCARAB", "WILD", "A"));
        final List<List<Integer>> paylines = List.of(
                List.of(1, 1, 1, 1, 1),
                List.of(0, 0, 0, 0, 0),
                List.of(2, 2, 2, 2, 2),
                List.of(0, 1, 2, 1, 0),
                List.of(2, 1, 0, 1, 2));
        final List<PaytableEntry> paytable = List.of(
                new PaytableEntry("ANUBIS", Map.of(3, 10L, 4, 50L, 5, 250L)),
                new PaytableEntry("SCARAB", Map.of(3, 5L, 4, 20L, 5, 100L)),
                new PaytableEntry("A", Map.of(3, 2L, 4, 10L, 5, 40L)));
        final Map<String, Map<Integer, Long>> scatterPays =
                Map.of("SCATTER", Map.of(2, 1L, 3, 5L, 4, 20L, 5, 100L));
        final WildSpec wild = new WildSpec(Set.of(SymbolKind.REGULAR));
        final FreeSpinsSpec freeSpins = new FreeSpinsSpec(
                "SCATTER", 3, Map.of(3, 8, 4, 12, 5, 20), 2, true);
        return new GameConfigSpec(5, 3, symbols, reels, paylines, paytable, scatterPays, wild, freeSpins);
    }

    /**
     * Same as {@link #egyptianSpec()} but with the free-spins retrigger disabled. The readme example
     * is illustrative, not balanced: its scatter frequency and award make the retrigger diverge
     * (positive drift → an unbounded cascade), which is fine for the engine (it must tolerate it,
     * AC6b) but useless as a finite test fixture. Used to exercise determinism over a bounded round.
     */
    static GameConfigSpec egyptianSpecBounded() {
        final GameConfigSpec base = egyptianSpec();
        final FreeSpinsSpec fs = base.freeSpins();
        final FreeSpinsSpec bounded = new FreeSpinsSpec(
                fs.triggerSymbol(), fs.minTriggerCount(), fs.award(), fs.multiplier(), false);
        return new GameConfigSpec(base.cols(), base.rows(), base.symbols(), base.reels(),
                base.paylines(), base.paytable(), base.scatterPays(), base.wild(), bounded);
    }
}
