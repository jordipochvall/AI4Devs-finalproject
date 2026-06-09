package com.novacasino.api.player.dto;

/**
 * One winning payline of a spin (readme §4.4.3 SpinResult).
 *
 * @param paylineIndex index of the payline in the config's {@code paylines}
 * @param symbol       the winning symbol id (the anchor; the best regular for an all-wild line)
 * @param count        number of matched symbols from column 0
 * @param winCents     prize for this line in cents (multiplier already applied)
 */
public record WinningPaylineDto(int paylineIndex, String symbol, int count, long winCents) {
}
