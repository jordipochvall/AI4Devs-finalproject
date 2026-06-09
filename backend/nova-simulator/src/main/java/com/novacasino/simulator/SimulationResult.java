package com.novacasino.simulator;

import java.util.List;
import java.util.Map;

/**
 * Aggregated outcome of a mass simulation (readme §2.1.7, §3.2.9). These are <strong>tools for the
 * mathematician</strong> (C3): the platform <em>measures</em> the empirical behaviour of the engine;
 * it does not declare the target RTP (that is the mathematician's {@code rtp_target}).
 *
 * @param numSpins                  number of base rounds simulated
 * @param betCents                  total bet per round, in cents
 * @param durationMs                wall-clock duration of the run
 * @param rtp                       empirical RTP (total win / total bet)
 * @param rtpStdError               standard error of the RTP estimate (stdev of per-round return / √N)
 * @param rtpBaseGame               RTP contributed by base-game wins
 * @param rtpFreeSpins              RTP contributed by free-spin wins
 * @param hitFrequency              fraction of rounds with a positive win
 * @param volatility                standard deviation of the per-round return (win/bet)
 * @param maxWinMultiplier          largest single-round win as a multiple of the bet
 * @param freeSpinTriggerFrequency  fraction of rounds that triggered free spins
 * @param longestDryStreak          longest run of consecutive zero-win rounds
 * @param prizeDistribution         histogram of rounds by win-multiplier bucket (label → count)
 * @param convergenceSample         RTP sampled at increasing spin counts (a single-thread sample path)
 * @param rtpBreakdown              RTP split base/free spins and contribution per symbol
 * @param reelSymbolFrequencies     per reel, the relative frequency of each symbol on its strip
 */
public record SimulationResult(
        long numSpins,
        long betCents,
        long durationMs,
        double rtp,
        double rtpStdError,
        double rtpBaseGame,
        double rtpFreeSpins,
        double hitFrequency,
        double volatility,
        double maxWinMultiplier,
        double freeSpinTriggerFrequency,
        long longestDryStreak,
        Map<String, Long> prizeDistribution,
        List<ConvergencePoint> convergenceSample,
        RtpBreakdown rtpBreakdown,
        List<Map<String, Double>> reelSymbolFrequencies) {

    /** One point of the convergence curve: the running RTP after {@code spins} rounds. */
    public record ConvergencePoint(long spins, double rtp) {
    }

    /**
     * RTP decomposition.
     *
     * @param baseGame  RTP from base-game wins
     * @param freeSpins RTP from free-spin wins
     * @param bySymbol  RTP contributed by each symbol's line wins (symbol id → RTP)
     */
    public record RtpBreakdown(double baseGame, double freeSpins, Map<String, Double> bySymbol) {
    }
}
