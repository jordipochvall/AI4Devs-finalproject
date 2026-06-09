package com.novacasino.simulator;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lock-free aggregator that combines the per-worker {@link CountingSink}s of a simulation
 * (readme §2.1.7). Additive metrics use {@link LongAdder}/{@link DoubleAdder} so several workers'
 * results combine without corruption (AC4); the maxima use a CAS loop. Each worker is merged once,
 * after it finishes its chunk.
 */
final class SimulationAccumulator {

    final LongAdder rounds = new LongAdder();
    final LongAdder freeSpinCount = new LongAdder();
    final LongAdder hitRounds = new LongAdder();
    final LongAdder triggerRounds = new LongAdder();
    final LongAdder totalWinCents = new LongAdder();
    final LongAdder baseWinCents = new LongAdder();
    final LongAdder freeWinCents = new LongAdder();
    final DoubleAdder sumReturn = new DoubleAdder();
    final DoubleAdder sumReturnSq = new DoubleAdder();
    final AtomicLong maxRoundWinCents = new AtomicLong();
    final AtomicLong longestDryStreak = new AtomicLong();
    final LongAdder[] histogram;
    final LongAdder[] bySymbolWinCents;

    SimulationAccumulator(final int bucketCount, final int symbolCount) {
        this.histogram = newAdders(bucketCount);
        this.bySymbolWinCents = newAdders(symbolCount);
    }

    /** Merges one worker's local sink into the shared totals. */
    void merge(final CountingSink sink) {
        rounds.add(sink.rounds);
        freeSpinCount.add(sink.freeSpinCount);
        hitRounds.add(sink.hitRounds);
        triggerRounds.add(sink.triggerRounds);
        totalWinCents.add(sink.totalWinCents);
        baseWinCents.add(sink.baseWinCents);
        freeWinCents.add(sink.freeWinCents);
        sumReturn.add(sink.sumReturn);
        sumReturnSq.add(sink.sumReturnSq);
        updateMax(maxRoundWinCents, sink.maxRoundWinCents);
        updateMax(longestDryStreak, sink.longestDryStreak);
        for (int i = 0; i < histogram.length; i++) {
            histogram[i].add(sink.histogram[i]);
        }
        for (int i = 0; i < bySymbolWinCents.length; i++) {
            bySymbolWinCents[i].add(sink.bySymbolWinCents[i]);
        }
    }

    private static LongAdder[] newAdders(final int n) {
        final LongAdder[] adders = new LongAdder[n];
        for (int i = 0; i < n; i++) {
            adders[i] = new LongAdder();
        }
        return adders;
    }

    /** CAS-based monotonic max. */
    private static void updateMax(final AtomicLong target, final long value) {
        long current;
        while (value > (current = target.get())) {
            if (target.compareAndSet(current, value)) {
                return;
            }
        }
    }
}
