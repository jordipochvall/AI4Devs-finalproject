package com.novacasino.domain.engine;

import com.novacasino.domain.rng.RngEngine;

/**
 * Data-oriented core of the slot engine and the single source of truth for a spin's outcome
 * (readme §2.1.7). Given a {@link CompiledGame}, an {@link RngEngine} and a {@link RoundSink}, it
 * resolves the base spin and the whole free-spins cascade (including unbounded retrigger) using
 * <strong>only primitives</strong>, <strong>integer arithmetic</strong> (for cross-platform
 * determinism) and <strong>zero allocations per spin</strong> (reused buffers). It does not return
 * rich objects: each spin/free spin is pushed to the sink.
 *
 * <p><strong>Not thread-safe.</strong> A kernel owns mutable buffers sized for one game; each
 * simulator worker must hold its own kernel (readme §2.1.7, invariant 3).
 *
 * <p><strong>RNG consumption order.</strong> One {@link RngEngine#nextInt(int)} per reel in column
 * order {@code 0 → cols-1}; free spins consume afterwards in deterministic order. Given a seed the
 * sequence is reproducible on any machine (enables golden-master verification and forensic replay).
 *
 * <h2>Prize rules (readme §3.3.3)</h2>
 * <ul>
 *   <li><b>lineBet</b> = {@code betCents / paylineCount} (all paylines active in the MVP);
 *       the caller guarantees {@code betCents % paylineCount == 0}.</li>
 *   <li><b>Line prize</b> = {@code multiplier × lineBet}; each line pays its single best combo,
 *       left to right from column 0. A wild substitutes any regular to maximize; a full wild line
 *       pays the highest-value regular.</li>
 *   <li><b>Scatter prize</b> = {@code multiplier × betCents}, by total count anywhere, independent
 *       of the free-spins trigger.</li>
 *   <li>During free spins every prize is multiplied by the feature {@code multiplier}.</li>
 * </ul>
 */
public final class SpinKernel {

    private final CompiledGame game;

    // --- reused buffers (allocated once, sized for this game) ---
    private final int[] window;              // column-major: window[col * rows + row]
    private final int[] winningLines;        // payline indices that won (first winningLineCount valid)
    private final int[] winningLineSymbols;  // anchor symbol id per winning line (parallel)
    private final int[] winningLineCounts;   // matched count per winning line (parallel)
    private final long[] winningLineWins;    // prize in cents per winning line (parallel)
    private final int[] symbolCounts;        // per-symbol occurrence tally for the current spin

    // --- scratch set by evaluateLine for the current line ---
    private int evalSymbol;
    private int evalCount;

    /**
     * Creates a kernel bound to one game. Buffers are allocated here, once.
     *
     * @param game the compiled game to play
     */
    public SpinKernel(final CompiledGame game) {
        this.game = game;
        this.window = new int[game.cols * game.rows];
        this.winningLines = new int[game.paylineCount];
        this.winningLineSymbols = new int[game.paylineCount];
        this.winningLineCounts = new int[game.paylineCount];
        this.winningLineWins = new long[game.paylineCount];
        this.symbolCounts = new int[game.symbolCount];
    }

    /**
     * Resolves a full round (base spin plus any free-spins cascade) for the given total bet,
     * pushing every spin to the sink.
     *
     * @param betCents total bet in cents; must be a non-negative multiple of {@code paylineCount}
     * @param rng      random source (consumed in column order)
     * @param sink     receiver of each spin's outcome
     * @throws IllegalArgumentException if {@code betCents} is negative or not a multiple of the payline count
     */
    public void spin(final long betCents, final RngEngine rng, final RoundSink sink) {
        final int paylineCount = game.paylineCount;
        if (betCents < 0 || (paylineCount > 0 && betCents % paylineCount != 0)) {
            throw new IllegalArgumentException(
                    "betCents must be a non-negative multiple of the payline count: " + betCents);
        }
        final long lineBet = paylineCount > 0 ? betCents / paylineCount : 0L;

        // Base spin (multiplier 1, not a free spin).
        final int scatterCount = resolveSpin(betCents, lineBet, rng, sink, false, 1);

        // Free-spins cascade with unbounded retrigger.
        if (game.hasFreeSpins && scatterCount >= game.fsMinTriggerCount) {
            final int multiplier = game.fsMultiplier;
            long remaining = game.fsAward[scatterCount];
            while (remaining > 0) {
                remaining--;
                final int sc = resolveSpin(betCents, lineBet, rng, sink, true, multiplier);
                if (game.fsRetrigger && sc >= game.fsMinTriggerCount) {
                    remaining += game.fsAward[sc];
                }
            }
        }
    }

    /**
     * Resolves a single spin: spins the reels, evaluates line and scatter prizes, applies the round
     * multiplier and emits the result. Returns the trigger-symbol scatter count (drives free spins).
     */
    private int resolveSpin(final long betCents, final long lineBet, final RngEngine rng,
                            final RoundSink sink, final boolean isFreeSpin, final int multiplier) {
        final int cols = game.cols;
        final int rows = game.rows;

        // 1. Spin each reel and fill the window with a circular slice from the stop position.
        final int[][] reels = game.reels;
        for (int c = 0; c < cols; c++) {
            final int[] strip = reels[c];
            final int len = strip.length;
            final int stop = rng.nextInt(len);
            final int base = c * rows;
            for (int r = 0; r < rows; r++) {
                window[base + r] = strip[(stop + r) % len];
            }
        }

        // 2. Tally symbol occurrences (for scatter prizes and the free-spins trigger).
        final int symbolCount = game.symbolCount;
        for (int i = 0; i < symbolCount; i++) {
            symbolCounts[i] = 0;
        }
        final int cells = cols * rows;
        for (int i = 0; i < cells; i++) {
            symbolCounts[window[i]]++;
        }

        // 3. Line prizes (best combo per line, over lineBet, with the round multiplier applied).
        long win = 0L;
        int winningLineCount = 0;
        final int[][] paylines = game.paylines;
        final int paylineCount = game.paylineCount;
        for (int l = 0; l < paylineCount; l++) {
            final long lineMultiplier = evaluateLine(paylines[l], rows);
            if (lineMultiplier > 0) {
                final long lineWin = lineMultiplier * lineBet * multiplier;
                winningLines[winningLineCount] = l;
                winningLineSymbols[winningLineCount] = evalSymbol;
                winningLineCounts[winningLineCount] = evalCount;
                winningLineWins[winningLineCount] = lineWin;
                winningLineCount++;
                win += lineMultiplier * lineBet;
            }
        }

        // 4. Scatter prizes (anywhere, by total count, over betCents).
        final int[] scatterPaySymbols = game.scatterPaySymbols;
        final long[][] scatterPayouts = game.scatterPayouts;
        for (final int sym : scatterPaySymbols) {
            final long scatterMultiplier = scatterPayouts[sym][symbolCounts[sym]];
            if (scatterMultiplier > 0) {
                win += scatterMultiplier * betCents;
            }
        }

        // 5. Apply the round multiplier to the scatter portion (1 on the base spin; line wins
        //    already had it applied above).
        win *= multiplier;

        // 6. Trigger-symbol scatter count, then emit.
        final int scatterCount = game.hasFreeSpins ? symbolCounts[game.fsTriggerSymbol] : 0;
        sink.onSpin(window, cols, rows,
                winningLines, winningLineSymbols, winningLineCounts, winningLineWins, winningLineCount,
                win, isFreeSpin, multiplier, scatterCount);
        return scatterCount;
    }

    /**
     * Evaluates one payline left to right from column 0 and returns its best line multiplier
     * ({@code 0} if it does not win), also setting {@link #evalSymbol} and {@link #evalCount} for the
     * winning combo. A wild substitutes the anchor regular to extend the run; a run made only of
     * wilds pays as the highest-value regular for its length.
     *
     * @param line the payline (row index per column)
     * @param rows the number of rows (to index the column-major window)
     * @return the line multiplier, or {@code 0} if there is no win
     */
    private long evaluateLine(final int[] line, final int rows) {
        final int cols = game.cols;
        final boolean[] wild = game.wild;
        final boolean[] scatter = game.scatter;

        // Skip leading wilds.
        int c = 0;
        int leadingWilds = 0;
        while (c < cols && wild[window[c * rows + line[c]]]) {
            leadingWilds++;
            c++;
        }
        if (c == cols) {
            // Whole line is wild → pays the highest-value regular at full length.
            evalSymbol = game.bestRegularSymbolByCount[cols];
            evalCount = cols;
            return game.bestRegularByCount[cols];
        }
        final int firstNonWild = window[c * rows + line[c]];
        if (scatter[firstNonWild]) {
            // Leading wild run cut short by a scatter → pay the wild run as the best regular for its length.
            if (leadingWilds == 0) {
                return 0L;
            }
            evalSymbol = game.bestRegularSymbolByCount[leadingWilds];
            evalCount = leadingWilds;
            return game.bestRegularByCount[leadingWilds];
        }
        // firstNonWild is a REGULAR → it anchors the combo; wilds extend it.
        final int anchor = firstNonWild;
        int count = leadingWilds;
        for (int k = c; k < cols; k++) {
            final int sym = window[k * rows + line[k]];
            if (sym == anchor || wild[sym]) {
                count++;
            } else {
                break;
            }
        }
        evalSymbol = anchor;
        evalCount = count;
        return game.linePayouts[anchor][count];
    }
}
