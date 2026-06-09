package com.novacasino.domain.engine;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Neutral, immutable representation of a game {@code config} (readme §3.3) consumed by the
 * {@link GameCompiler}. It deliberately uses only JDK collections so that {@code nova-domain}
 * stays free of any JSON/Spring dependency: the mapping from the persisted {@code JSONB} to this
 * spec is performed in an outer layer (see {@code HU-1-BE-02}).
 *
 * <p>All multipliers are integers ({@code long}) so the engine can stay on integer arithmetic
 * (determinism, see readme §2.1.7).
 *
 * @param cols        number of reels / columns ({@code grid.cols})
 * @param rows        number of visible rows ({@code grid.rows})
 * @param symbols     symbol catalogue (id + kind); order is irrelevant, ids are unique
 * @param reels       one reel strip per column ({@code reels[col]} = ordered list of symbol ids)
 * @param paylines    each payline as the row index it occupies per column ({@code paylines[line][col]})
 * @param paytable    line payouts for {@code REGULAR} symbols (over {@code lineBet})
 * @param scatterPays "anywhere" payouts per {@code SCATTER} symbol by total count (over {@code betCents});
 *                    may be empty
 * @param wild        wild substitution rule, or {@code null} if the game has no wild
 * @param freeSpins   free-spins feature, or {@code null} if the game has none
 */
public record GameConfigSpec(
        int cols,
        int rows,
        List<SymbolSpec> symbols,
        List<List<String>> reels,
        List<List<Integer>> paylines,
        List<PaytableEntry> paytable,
        Map<String, Map<Integer, Long>> scatterPays,
        WildSpec wild,
        FreeSpinsSpec freeSpins) {

    /** A symbol of the game: a unique {@code id} and its {@link SymbolKind}. */
    public record SymbolSpec(String id, SymbolKind kind) {
    }

    /**
     * A paytable row for one {@code REGULAR} symbol.
     *
     * @param symbol  the symbol id
     * @param payouts map of {@code consecutive-count -> multiplier} (over {@code lineBet})
     */
    public record PaytableEntry(String symbol, Map<Integer, Long> payouts) {
    }

    /**
     * Wild substitution rule.
     *
     * @param substitutes the {@link SymbolKind}s the wild can replace (MVP: {@code REGULAR})
     */
    public record WildSpec(Set<SymbolKind> substitutes) {
    }

    /**
     * Free-spins feature (readme §3.3.1).
     *
     * @param triggerSymbol   the {@code SCATTER} symbol id whose count triggers the feature
     * @param minTriggerCount minimum scatter count to trigger / retrigger
     * @param award           map of {@code scatterCount -> number of free spins}
     * @param multiplier      multiplier applied to every prize obtained during the free-spins round
     * @param retrigger       whether reaching {@code minTriggerCount} during a free spin grants more spins
     */
    public record FreeSpinsSpec(
            String triggerSymbol,
            int minTriggerCount,
            Map<Integer, Integer> award,
            int multiplier,
            boolean retrigger) {
    }
}
