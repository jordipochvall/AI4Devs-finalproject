package com.novacasino.simulator;

import com.novacasino.domain.engine.RoundSink;

/**
 * Simulator-side {@link RoundSink}: the counterpart of production's {@code MaterializingSink}
 * (readme §2.1.7). It <strong>aggregates and discards</strong> — it never copies nor retains the
 * kernel's buffers, so the 10M-spin loop does not allocate per spin (AC3). It is round-aware: the
 * kernel emits a base spin followed by its free spins on the same thread, so this sink accumulates a
 * round and flushes it on the next base spin (and once at the end), yielding round-level metrics.
 *
 * <p>One instance per worker; all state is local primitives + two preallocated arrays. The
 * {@link SimulationAccumulator} merges workers' instances afterwards.
 */
final class CountingSink implements RoundSink {

    /** Upper bounds (inclusive) of the win-multiplier histogram buckets; last bucket is the overflow. */
    static final double[] BUCKET_BOUNDS = {0, 1, 2, 5, 10, 20, 50, 100};
    static final String[] BUCKET_LABELS = {"0", "(0,1]", "(1,2]", "(2,5]", "(5,10]",
            "(10,20]", "(20,50]", "(50,100]", ">100"};

    private final long betCents;
    private final int triggerThreshold; // min scatters that trigger free spins (0 = no feature)

    // --- round aggregates ---
    long rounds;
    long freeSpinCount;
    long hitRounds;
    long triggerRounds;
    long totalWinCents;
    long baseWinCents;
    long freeWinCents;
    double sumReturn;     // Σ (roundWin / bet)
    double sumReturnSq;   // Σ (roundWin / bet)²
    long maxRoundWinCents;
    long longestDryStreak;
    final long[] histogram = new long[BUCKET_LABELS.length];
    final long[] bySymbolWinCents;

    // --- current-round state ---
    private boolean inRound;
    private long currentRoundWin;
    private long currentDryStreak;

    CountingSink(final long betCents, final int triggerThreshold, final int symbolCount) {
        this.betCents = betCents;
        this.triggerThreshold = triggerThreshold;
        this.bySymbolWinCents = new long[symbolCount];
    }

    @Override
    public void onSpin(final int[] window, final int cols, final int rows,
                       final int[] winningLines, final int[] winningLineSymbols,
                       final int[] winningLineCounts, final long[] winningLineWins,
                       final int winningLineCount, final long winCents, final boolean freeSpin,
                       final int multiplier, final int scatterCount) {
        if (!freeSpin) {
            flushRound();           // close the previous round
            inRound = true;
            currentRoundWin = winCents;
            baseWinCents += winCents;
            if (triggerThreshold > 0 && scatterCount >= triggerThreshold) {
                triggerRounds++;
            }
        } else {
            currentRoundWin += winCents;
            freeWinCents += winCents;
            freeSpinCount++;
        }
        totalWinCents += winCents;

        // Per-symbol line-win attribution (no allocation: indexed into a preallocated array).
        for (int i = 0; i < winningLineCount; i++) {
            bySymbolWinCents[winningLineSymbols[i]] += winningLineWins[i];
        }
    }

    /** Closes the open round, updating round-level metrics. Call once more after the chunk ends. */
    void flushRound() {
        if (!inRound) {
            return;
        }
        rounds++;
        final long rw = currentRoundWin;
        final double r = rw / (double) betCents;
        sumReturn += r;
        sumReturnSq += r * r;
        if (rw > 0) {
            hitRounds++;
            currentDryStreak = 0;
        } else {
            currentDryStreak++;
            if (currentDryStreak > longestDryStreak) {
                longestDryStreak = currentDryStreak;
            }
        }
        if (rw > maxRoundWinCents) {
            maxRoundWinCents = rw;
        }
        histogram[bucketOf(r)]++;
        inRound = false;
    }

    /** Index of the histogram bucket for a win multiplier. */
    static int bucketOf(final double multiplier) {
        for (int i = 0; i < BUCKET_BOUNDS.length; i++) {
            if (multiplier <= BUCKET_BOUNDS[i]) {
                return i;
            }
        }
        return BUCKET_LABELS.length - 1; // overflow: > last bound
    }
}
