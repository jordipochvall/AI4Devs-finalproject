package com.novacasino.common.dto;

import java.util.List;

/**
 * Outcome of a spin returned by {@code POST /player/games/{gameId}/spin} (readme §4.4.3).
 * Free spins are resolved atomically with the triggering spin and nested in {@code freeSpins.rounds}.
 *
 * @param roundId          id of the persisted base round
 * @param betCents         total bet of the spin
 * @param lineBetCents     bet per line ({@code betCents / number of paylines})
 * @param winCents         total prize of the round in cents (base spin + every free spin)
 * @param balancePreCents  balance before the bet
 * @param balancePostCents balance after the bet and all prizes ({@code pre - bet + win})
 * @param view             visible symbols, one sub-list per column
 * @param winningPaylines  winning paylines of this spin
 * @param scatterCount     number of trigger-symbol scatters in {@code view}
 * @param freeSpins        free-spins summary and the resolved free-spin rounds
 */
public record SpinResultDto(
        Long roundId,
        long betCents,
        long lineBetCents,
        long winCents,
        long balancePreCents,
        long balancePostCents,
        List<List<String>> view,
        List<WinningPaylineDto> winningPaylines,
        int scatterCount,
        FreeSpinsDto freeSpins) {

    /**
     * Free-spins summary.
     *
     * @param triggered whether the spin triggered free spins
     * @param awarded   number of free spins initially awarded
     * @param rounds    the resolved free-spin rounds (each a full {@link SpinResultDto})
     */
    public record FreeSpinsDto(boolean triggered, int awarded, List<SpinResultDto> rounds) {
    }
}
