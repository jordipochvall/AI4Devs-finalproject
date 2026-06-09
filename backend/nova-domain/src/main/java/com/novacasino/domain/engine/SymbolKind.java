package com.novacasino.domain.engine;

/**
 * Kind of a slot symbol (readme §3.3.1).
 *
 * <ul>
 *   <li>{@code REGULAR} — pays on a payline (left to right) according to the paytable.</li>
 *   <li>{@code WILD} — substitutes {@code REGULAR} symbols to maximize the line prize; never pays by itself
 *       except as the best regular when a whole line is wild.</li>
 *   <li>{@code SCATTER} — pays "anywhere" by total count (scatterPays) and/or triggers free spins.</li>
 * </ul>
 */
public enum SymbolKind {
    REGULAR,
    WILD,
    SCATTER
}
