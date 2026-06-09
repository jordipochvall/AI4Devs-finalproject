package com.novacasino.simulator;

import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.SpinKernel;
import com.novacasino.domain.rng.RngEngine;
import com.novacasino.domain.rng.RngFactory;
import com.novacasino.simulator.SimulationResult.ConvergencePoint;
import com.novacasino.simulator.SimulationResult.RtpBreakdown;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

/**
 * Runs a mass simulation of a {@link CompiledGame} by executing the <strong>same
 * {@link SpinKernel}</strong> as production over a {@link CountingSink} (readme §2.1.7). The
 * {@code numSpins} base rounds are split across a {@link ForkJoinPool}: the compiled game is
 * immutable and shared read-only, while each worker owns its own kernel, {@link RngEngine} and
 * counting sink — so there is no per-spin allocation and no shared mutable state in the hot loop.
 *
 * <p>It never touches a database, wallet or transaction (AC2): everything runs in memory and only
 * the aggregated {@link SimulationResult} is returned, for the caller to persist.
 */
public final class SimulationRunner {

    /** Cap on the (single-thread) convergence sample so it stays cheap regardless of {@code numSpins}. */
    private static final long CONVERGENCE_CAP = 200_000L;

    private final RngFactory rngFactory;
    private final int parallelism;

    public SimulationRunner(final RngFactory rngFactory) {
        this(rngFactory, Math.max(1, Runtime.getRuntime().availableProcessors()));
    }

    SimulationRunner(final RngFactory rngFactory, final int parallelism) {
        this.rngFactory = rngFactory;
        this.parallelism = parallelism;
    }

    /**
     * Simulates {@code numSpins} base rounds at {@code betCents} and returns the aggregated metrics.
     *
     * @param game     the compiled game (immutable, shared by all workers)
     * @param numSpins number of base rounds to simulate (> 0)
     * @param betCents total bet per round in cents (multiple of the payline count)
     * @param seed     base RNG seed; each worker derives a distinct seed from it (reproducible)
     * @return the aggregated {@link SimulationResult}
     */
    public SimulationResult run(final CompiledGame game, final long numSpins, final long betCents,
                               final long seed) {
        if (numSpins <= 0) {
            throw new IllegalArgumentException("numSpins must be > 0: " + numSpins);
        }
        if (betCents <= 0 || betCents % game.paylineCount() != 0) {
            throw new IllegalArgumentException(
                    "betCents must be a positive multiple of the payline count: " + betCents);
        }

        final long start = System.nanoTime();
        final int threshold = game.hasFreeSpins() ? game.freeSpinMinTriggerCount() : 0;
        final SimulationAccumulator acc =
                new SimulationAccumulator(CountingSink.BUCKET_LABELS.length, game.symbolCount());

        final int workers = (int) Math.min(parallelism, numSpins);
        try (ForkJoinPool pool = new ForkJoinPool(workers)) {
            final List<Future<CountingSink>> futures = new ArrayList<>(workers);
            for (int w = 0; w < workers; w++) {
                final long chunk = chunkSize(numSpins, workers, w);
                final long workerSeed = seed + w;
                final Callable<CountingSink> task = () -> runChunk(game, chunk, betCents, threshold, workerSeed);
                futures.add(pool.submit(task));
            }
            for (final Future<CountingSink> future : futures) {
                acc.merge(future.get());
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Simulation interrupted", e);
        } catch (final ExecutionException e) {
            throw new IllegalStateException("Simulation worker failed", e.getCause());
        }

        final List<ConvergencePoint> convergence =
                sampleConvergence(game, Math.min(numSpins, CONVERGENCE_CAP), betCents, seed - 1);
        final long durationMs = (System.nanoTime() - start) / 1_000_000L;
        return assemble(game, numSpins, betCents, durationMs, acc, convergence);
    }

    // -------------------------------------------------------------------------

    /** Runs one worker's chunk on its own kernel/RNG/sink and returns the sink. */
    private CountingSink runChunk(final CompiledGame game, final long chunk, final long betCents,
                                  final int threshold, final long workerSeed) {
        final SpinKernel kernel = new SpinKernel(game);
        final RngEngine rng = rngFactory.create(workerSeed);
        final CountingSink sink = new CountingSink(betCents, threshold, game.symbolCount());
        for (long i = 0; i < chunk; i++) {
            kernel.spin(betCents, rng, sink);
        }
        sink.flushRound();
        return sink;
    }

    /** Single-thread pass recording the running RTP at geometric checkpoints (a valid sample path). */
    private List<ConvergencePoint> sampleConvergence(final CompiledGame game, final long sampleSpins,
                                                     final long betCents, final long seed) {
        final SpinKernel kernel = new SpinKernel(game);
        final RngEngine rng = rngFactory.create(seed);
        final CountingSink sink = new CountingSink(betCents, 0, game.symbolCount());
        final List<ConvergencePoint> points = new ArrayList<>();
        long nextCheckpoint = 100L;
        for (long i = 1; i <= sampleSpins; i++) {
            kernel.spin(betCents, rng, sink);
            if (i == nextCheckpoint || i == sampleSpins) {
                // Close the in-flight round to read a consistent running RTP.
                final long winSoFar = sink.totalWinCents;
                points.add(new ConvergencePoint(i, winSoFar / (double) (i * betCents)));
                nextCheckpoint = Math.min(sampleSpins, nextCheckpoint * 2);
            }
        }
        return points;
    }

    /** Builds the immutable result from the aggregated counters and the compiled game. */
    private SimulationResult assemble(final CompiledGame game, final long numSpins, final long betCents,
                                      final long durationMs, final SimulationAccumulator acc,
                                      final List<ConvergencePoint> convergence) {
        final long rounds = acc.rounds.sum();
        final double totalBet = (double) rounds * betCents;
        final double rtp = acc.totalWinCents.sum() / totalBet;
        final double rtpBase = acc.baseWinCents.sum() / totalBet;
        final double rtpFree = acc.freeWinCents.sum() / totalBet;

        // mean per-round return == rtp; variance from Σr and Σr².
        final double meanReturn = acc.sumReturn.sum() / rounds;
        final double variance = Math.max(0.0, acc.sumReturnSq.sum() / rounds - meanReturn * meanReturn);
        final double volatility = Math.sqrt(variance);
        final double rtpStdError = volatility / Math.sqrt(rounds);

        final Map<String, Long> prizeDistribution = new LinkedHashMap<>();
        for (int i = 0; i < CountingSink.BUCKET_LABELS.length; i++) {
            prizeDistribution.put(CountingSink.BUCKET_LABELS[i], acc.histogram[i].sum());
        }

        final Map<String, Double> bySymbol = new LinkedHashMap<>();
        for (int s = 0; s < game.symbolCount(); s++) {
            final long won = acc.bySymbolWinCents[s].sum();
            if (won > 0) {
                bySymbol.put(game.symbolId(s), won / totalBet);
            }
        }

        return new SimulationResult(
                rounds,
                betCents,
                durationMs,
                rtp,
                rtpStdError,
                rtpBase,
                rtpFree,
                acc.hitRounds.sum() / (double) rounds,
                volatility,
                acc.maxRoundWinCents.get() / (double) betCents,
                acc.triggerRounds.sum() / (double) rounds,
                acc.longestDryStreak.get(),
                prizeDistribution,
                convergence,
                new RtpBreakdown(rtpBase, rtpFree, bySymbol),
                reelSymbolFrequencies(game));
    }

    /** Per reel, the relative frequency of each symbol on its strip (a reel-strip statistic). */
    private List<Map<String, Double>> reelSymbolFrequencies(final CompiledGame game) {
        final List<Map<String, Double>> perReel = new ArrayList<>(game.reelCount());
        for (int reel = 0; reel < game.reelCount(); reel++) {
            final int length = game.reelLength(reel);
            final long[] counts = new long[game.symbolCount()];
            for (int pos = 0; pos < length; pos++) {
                counts[game.reelSymbolAt(reel, pos)]++;
            }
            final Map<String, Double> freq = new LinkedHashMap<>();
            for (int s = 0; s < counts.length; s++) {
                if (counts[s] > 0) {
                    freq.put(game.symbolId(s), counts[s] / (double) length);
                }
            }
            perReel.add(freq);
        }
        return perReel;
    }

    /** Even split of {@code total} rounds across {@code workers}, giving the remainder to the first ones. */
    private static long chunkSize(final long total, final int workers, final int index) {
        final long base = total / workers;
        final long remainder = total % workers;
        return base + (index < remainder ? 1 : 0);
    }
}
