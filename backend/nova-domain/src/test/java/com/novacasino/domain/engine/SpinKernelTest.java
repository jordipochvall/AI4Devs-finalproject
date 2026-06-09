package com.novacasino.domain.engine;

import com.novacasino.domain.engine.EngineTestSupport.CyclingRng;
import com.novacasino.domain.engine.EngineTestSupport.NoOpSink;
import com.novacasino.domain.engine.EngineTestSupport.RecordingSink;
import com.novacasino.domain.engine.EngineTestSupport.ScriptedRng;
import com.novacasino.domain.engine.EngineTestSupport.SeededRng;
import com.novacasino.domain.engine.EngineTestSupport.Spin;
import com.novacasino.domain.engine.GameConfigSpec.FreeSpinsSpec;
import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.engine.GameConfigSpec.WildSpec;
import com.novacasino.domain.rng.RngEngine;
import com.sun.management.ThreadMXBean;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * HU-1-BE-01 — behaviour of the {@link SpinKernel}: integer prize math (AC2/AC5), wild evaluation
 * and free-spins cascade (AC6/AC6b), determinism (AC3) and zero per-spin allocation (AC4).
 *
 * <p>The controlled games use 3-symbol reel strips so that, with every stop at {@code 0}, the
 * visible window equals the reels — making the expected outcome obvious by construction.
 */
class SpinKernelTest {

    private static final SymbolSpec A = new SymbolSpec("A", SymbolKind.REGULAR);
    private static final SymbolSpec B = new SymbolSpec("B", SymbolKind.REGULAR);
    private static final SymbolSpec W = new SymbolSpec("W", SymbolKind.WILD);
    private static final SymbolSpec S = new SymbolSpec("S", SymbolKind.SCATTER);
    private static final List<PaytableEntry> AB_PAYTABLE = List.of(
            new PaytableEntry("A", Map.of(3, 5L)),
            new PaytableEntry("B", Map.of(3, 2L)));
    /** Single payline along the middle row. */
    private static final List<List<Integer>> MIDDLE_LINE = List.of(List.of(1, 1, 1));

    private final GameCompiler compiler = new GameCompiler();

    // ----------------------------------------------------------------- prize math (AC2/AC5)

    @Test
    void payToLineOverLineBet() {
        // Middle row is A,A,A → A pays 5 over the line bet (= total bet, single payline).
        final GameConfigSpec spec = new GameConfigSpec(3, 3,
                List.of(A, B),
                List.of(List.of("B", "A", "B"), List.of("B", "A", "B"), List.of("B", "A", "B")),
                MIDDLE_LINE, AB_PAYTABLE, Map.of(), null, null);
        final RecordingSink sink = play(spec, 300L, new ScriptedRng(0, 0, 0));

        assertThat(sink.spins).hasSize(1);
        final Spin spin = sink.spins.get(0);
        assertThat(spin.winCents()).isEqualTo(1500L); // 5 × lineBet(300)
        assertThat(spin.winningLines()).containsExactly(0);
        assertThat(spin.freeSpin()).isFalse();
        assertThat(spin.multiplier()).isEqualTo(1);
        assertThat(spin.scatterCount()).isZero();
    }

    // ----------------------------------------------------------------- wild evaluation (AC6/AC6b)

    @Test
    void wildSubstitutesToExtendTheLine() {
        // Middle row W,A,A → the wild anchors as A, paying a 3-of-a-kind.
        final GameConfigSpec spec = new GameConfigSpec(3, 3,
                List.of(A, B, W),
                List.of(List.of("B", "W", "B"), List.of("B", "A", "B"), List.of("B", "A", "B")),
                MIDDLE_LINE, AB_PAYTABLE, Map.of(), new WildSpec(Set.of(SymbolKind.REGULAR)), null);
        final RecordingSink sink = play(spec, 100L, new ScriptedRng(0, 0, 0));

        assertThat(sink.spins.get(0).winCents()).isEqualTo(500L); // 5 × 100
    }

    @Test
    void fullWildLinePaysTheBestRegular() {
        // Middle row W,W,W → pays as the highest-value regular (A = 5, over B = 2).
        final GameConfigSpec spec = new GameConfigSpec(3, 3,
                List.of(A, B, W),
                List.of(List.of("B", "W", "B"), List.of("B", "W", "B"), List.of("B", "W", "B")),
                MIDDLE_LINE, AB_PAYTABLE, Map.of(), new WildSpec(Set.of(SymbolKind.REGULAR)), null);
        final RecordingSink sink = play(spec, 100L, new ScriptedRng(0, 0, 0));

        assertThat(sink.spins.get(0).winCents()).isEqualTo(500L);
    }

    // ----------------------------------------------------------------- scatter independence (AC6)

    @Test
    void scatterPaysAnywhereOverTotalBetIndependentlyOfFreeSpins() {
        // Row 0 = S,S,S (scatter pays) and middle row = A,A,A (line win); no free spins configured.
        final GameConfigSpec spec = new GameConfigSpec(3, 3,
                List.of(A, B, S),
                List.of(List.of("S", "A", "B"), List.of("S", "A", "B"), List.of("S", "A", "B")),
                MIDDLE_LINE, AB_PAYTABLE, Map.of("S", Map.of(3, 10L)), null, null);
        final RecordingSink sink = play(spec, 100L, new ScriptedRng(0, 0, 0));

        assertThat(sink.spins).hasSize(1); // 3 scatters but no free-spins feature → no extra spins
        // 5 × lineBet(100) for the line + 10 × betCents(100) for the scatter.
        assertThat(sink.spins.get(0).winCents()).isEqualTo(500L + 1000L);
    }

    // ----------------------------------------------------------------- free spins (AC6/AC6b)

    @Test
    void freeSpinsMultiplierAppliesToEveryFreeSpinPrize() {
        final GameConfigSpec spec = freeSpinsGame(false);
        // base: 3 scatters → award 2 free spins; free spins land an A line, ×3 multiplier.
        final RecordingSink sink = play(spec, 100L, new ScriptedRng(0, 0, 0, 2, 2, 2, 2, 2, 2));

        assertThat(sink.spins).hasSize(3);
        assertThat(sink.spins.get(0).freeSpin()).isFalse();
        assertThat(sink.spins.get(0).scatterCount()).isEqualTo(3);
        assertThat(sink.spins.get(0).winCents()).isZero();
        for (int i = 1; i <= 2; i++) {
            assertThat(sink.spins.get(i).freeSpin()).isTrue();
            assertThat(sink.spins.get(i).multiplier()).isEqualTo(3);
            assertThat(sink.spins.get(i).winCents()).isEqualTo(1500L); // 3 × 5 × lineBet(100)
        }
    }

    @Test
    void retriggerGrantsMoreFreeSpinsWithoutBound() {
        final GameConfigSpec spec = freeSpinsGame(true);
        // base trigger (award 2); first free spin retriggers (+2); the rest land A lines.
        final RecordingSink sink = play(spec, 100L,
                new ScriptedRng(0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2));

        assertThat(sink.spins).hasSize(5); // base + 4 free spins (2 awarded + 2 retriggered)
        final long freeSpins = sink.spins.stream().filter(Spin::freeSpin).count();
        assertThat(freeSpins).isEqualTo(4);
    }

    // ----------------------------------------------------------------- determinism (AC3)

    @Test
    void sameSeedYieldsTheExactSameSequence() {
        final GameConfigSpec spec = EngineTestSupport.egyptianSpecBounded();
        final RecordingSink first = play(spec, 500L, new SeededRng(123_456_789L));
        final RecordingSink second = play(spec, 500L, new SeededRng(123_456_789L));

        assertThat(first.spins).hasSameSizeAs(second.spins);
        assertThat(first.spins).isNotEmpty();
        for (int i = 0; i < first.spins.size(); i++) {
            final Spin x = first.spins.get(i);
            final Spin y = second.spins.get(i);
            assertThat(x.window()).containsExactly(y.window());
            assertThat(x.winningLines()).containsExactly(y.winningLines());
            assertThat(x.winCents()).isEqualTo(y.winCents());
            assertThat(x.freeSpin()).isEqualTo(y.freeSpin());
            assertThat(x.multiplier()).isEqualTo(y.multiplier());
            assertThat(x.scatterCount()).isEqualTo(y.scatterCount());
        }
    }

    // ----------------------------------------------------------------- guard

    @Test
    void rejectsBetNotDivisibleByPaylineCount() {
        final CompiledGame game = compiler.compile(99L, EngineTestSupport.egyptianSpec());
        final SpinKernel kernel = new SpinKernel(game);

        assertThatThrownBy(() -> kernel.spin(101L, new ScriptedRng(0, 0, 0, 0, 0), new NoOpSink()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ----------------------------------------------------------------- zero allocation (AC4)

    @Test
    void steadyStateSpinDoesNotAllocate() {
        final ThreadMXBean threadBean = allocationBeanOrSkip();

        final GameConfigSpec spec = new GameConfigSpec(3, 3,
                List.of(A, B),
                List.of(List.of("B", "A", "B"), List.of("B", "A", "B"), List.of("B", "A", "B")),
                MIDDLE_LINE, AB_PAYTABLE, Map.of(), null, null);
        final CompiledGame game = compiler.compile(7L, spec);
        final SpinKernel kernel = new SpinKernel(game);
        final RngEngine rng = new CyclingRng(0);
        final NoOpSink sink = new NoOpSink();

        // Warm up the JIT so we measure the steady state, not first-call compilation.
        for (int i = 0; i < 50_000; i++) {
            kernel.spin(300L, rng, sink);
        }

        final long threadId = Thread.currentThread().threadId();
        final int spins = 200_000;
        final long before = threadBean.getThreadAllocatedBytes(threadId);
        for (int i = 0; i < spins; i++) {
            kernel.spin(300L, rng, sink);
        }
        final long allocated = threadBean.getThreadAllocatedBytes(threadId) - before;

        // A per-spin allocation would be megabytes over 200k spins; zero-alloc stays near nothing.
        assertThat(allocated).isLessThan(100_000L);
    }

    // ----------------------------------------------------------------- helpers

    /** Builds a 3x3 single-line free-spins game: strips [A,S,A,A]; stop 0 → 3 scatters, stop 2 → A line. */
    private static GameConfigSpec freeSpinsGame(final boolean retrigger) {
        final List<String> strip = List.of("A", "S", "A", "A");
        return new GameConfigSpec(3, 3,
                List.of(A, S),
                List.of(strip, strip, strip),
                MIDDLE_LINE,
                List.of(new PaytableEntry("A", Map.of(3, 5L))),
                Map.of(),
                null,
                new FreeSpinsSpec("S", 3, Map.of(3, 2), 3, retrigger));
    }

    private RecordingSink play(final GameConfigSpec spec, final long betCents, final RngEngine rng) {
        final CompiledGame game = compiler.compile(System.nanoTime(), spec); // unique id per call
        final SpinKernel kernel = new SpinKernel(game);
        final RecordingSink sink = new RecordingSink();
        kernel.spin(betCents, rng, sink);
        return sink;
    }

    private static ThreadMXBean allocationBeanOrSkip() {
        final java.lang.management.ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        assumeTrue(bean instanceof ThreadMXBean, "thread allocation metrics unavailable on this JVM");
        final ThreadMXBean sunBean = (ThreadMXBean) bean;
        assumeTrue(sunBean.isThreadAllocatedMemorySupported(), "thread allocation metrics unsupported");
        sunBean.setThreadAllocatedMemoryEnabled(true);
        return sunBean;
    }
}
