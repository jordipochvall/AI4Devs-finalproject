package com.novacasino.simulator;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * HU-2-QA-01 · AC4 — the accumulator aggregates the workers' results without corruption. Merging the
 * same set of {@link CountingSink}s concurrently from many threads must yield exactly the same totals
 * (and the same derived variance / standard error) as merging them sequentially. {@code LongAdder}
 * sums are exact; the {@code DoubleAdder} sums may differ only by floating-point ordering, so they
 * are compared within a tiny tolerance.
 */
class SimulationAccumulatorTest {

    private static final long BET = 100L;
    private static final int SYMBOLS = 2;
    private static final int BUCKETS = CountingSink.BUCKET_LABELS.length;
    private static final int[] NONE = new int[0];
    private static final long[] NONE_L = new long[0];

    @Test
    void concurrentMergeMatchesSequentialMerge() throws InterruptedException {
        final List<CountingSink> sinks = buildSinks(64, 2_000);

        // Sequential reference.
        final SimulationAccumulator sequential = new SimulationAccumulator(BUCKETS, SYMBOLS);
        sinks.forEach(sequential::merge);

        // Concurrent merge of the same sinks.
        final SimulationAccumulator concurrent = new SimulationAccumulator(BUCKETS, SYMBOLS);
        mergeConcurrently(concurrent, sinks);

        assertThat(concurrent.rounds.sum()).isEqualTo(sequential.rounds.sum());
        assertThat(concurrent.totalWinCents.sum()).isEqualTo(sequential.totalWinCents.sum());
        assertThat(concurrent.hitRounds.sum()).isEqualTo(sequential.hitRounds.sum());
        assertThat(concurrent.maxRoundWinCents.get()).isEqualTo(sequential.maxRoundWinCents.get());
        assertThat(concurrent.longestDryStreak.get()).isEqualTo(sequential.longestDryStreak.get());
        for (int i = 0; i < BUCKETS; i++) {
            assertThat(concurrent.histogram[i].sum()).isEqualTo(sequential.histogram[i].sum());
        }

        // Variance / standard error derived from the (double) sums must agree within FP tolerance.
        assertThat(stdError(concurrent)).isCloseTo(stdError(sequential), within(1e-9));
    }

    @Test
    void standardErrorMatchesAReferenceComputation() {
        // Deterministic dataset: one sink with known per-round returns.
        final CountingSink sink = new CountingSink(BET, 0, SYMBOLS);
        final long[] wins = {0, 100, 500, 0, 200, 0, 100, 1000, 0, 0}; // returns: win/bet
        for (final long win : wins) {
            base(sink, win);
        }
        sink.flushRound();

        final SimulationAccumulator acc = new SimulationAccumulator(BUCKETS, SYMBOLS);
        acc.merge(sink);

        // Reference: mean and population std dev of win/bet, std error = stdev/√n.
        final double n = wins.length;
        double sum = 0;
        for (final long w : wins) {
            sum += w / (double) BET;
        }
        final double mean = sum / n;
        double sq = 0;
        for (final long w : wins) {
            final double r = w / (double) BET;
            sq += (r - mean) * (r - mean);
        }
        final double refStdError = Math.sqrt(sq / n) / Math.sqrt(n);

        assertThat(acc.rounds.sum()).isEqualTo((long) n);
        assertThat(stdError(acc)).isCloseTo(refStdError, within(1e-9));
    }

    // -------------------------------------------------------------------------

    private List<CountingSink> buildSinks(final int count, final int roundsPerSink) {
        final Random random = new Random(12345L);
        final long[] possibleWins = {0, 0, 0, 100, 200, 500, 1000};
        final List<CountingSink> sinks = new ArrayList<>(count);
        for (int s = 0; s < count; s++) {
            final CountingSink sink = new CountingSink(BET, 0, SYMBOLS);
            for (int r = 0; r < roundsPerSink; r++) {
                base(sink, possibleWins[random.nextInt(possibleWins.length)]);
            }
            sink.flushRound();
            sinks.add(sink);
        }
        return sinks;
    }

    private static void base(final CountingSink sink, final long win) {
        sink.onSpin(NONE, 0, 0, NONE, NONE, NONE, NONE_L, 0, win, false, 1, 0);
    }

    private void mergeConcurrently(final SimulationAccumulator acc, final List<CountingSink> sinks)
            throws InterruptedException {
        final int threads = 8;
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        final CountDownLatch start = new CountDownLatch(1);
        try {
            for (int t = 0; t < threads; t++) {
                final int worker = t;
                pool.submit(() -> {
                    try {
                        start.await();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    for (int i = worker; i < sinks.size(); i += threads) {
                        acc.merge(sinks.get(i));
                    }
                });
            }
            start.countDown(); // release all threads at once to maximise contention
            pool.shutdown();
            assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            pool.shutdownNow();
        }
    }

    /** Standard error = stdev(return)/√n, from the accumulator's running sums. */
    private static double stdError(final SimulationAccumulator acc) {
        final double n = acc.rounds.sum();
        final double mean = acc.sumReturn.sum() / n;
        final double variance = Math.max(0.0, acc.sumReturnSq.sum() / n - mean * mean);
        return Math.sqrt(variance) / Math.sqrt(n);
    }
}
