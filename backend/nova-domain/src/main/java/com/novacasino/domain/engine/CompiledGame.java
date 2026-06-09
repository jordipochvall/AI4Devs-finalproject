package com.novacasino.domain.engine;

/**
 * Immutable, primitive representation of a game ready to be played by the {@link SpinKernel}.
 * Produced once per {@code config} by the {@link GameCompiler} and cached by {@code configId}
 * (game configs are immutable, so the cache is permanent — readme §2.1.7).
 *
 * <p>Everything is expressed in primitives so the kernel never allocates nor interprets JSON per
 * spin: symbols are dense {@code int} ids, reels/paylines are {@code int[][]} and payouts are
 * {@code long[][]}. The fields are package-private and read directly by the {@link SpinKernel} for
 * speed; outer layers use the public accessors.
 *
 * <p><strong>Window layout.</strong> The visible grid is addressed column-major:
 * {@code index = col * rows + row}.
 */
public final class CompiledGame {

    // --- identity & dimensions ---
    final long configId;
    final int cols;
    final int rows;
    final int symbolCount;
    final int paylineCount;
    /** Maximum number of a single symbol that can be visible at once ({@code cols * rows}). */
    final int maxSymbolCount;

    // --- symbol catalogue (indexed by dense symbol id) ---
    final String[] symbolIds;
    final SymbolKind[] kinds;
    final boolean[] wild;       // wild[id] == true if the symbol is a WILD
    final boolean[] scatter;    // scatter[id] == true if the symbol is a SCATTER

    // --- structure ---
    final int[][] reels;        // reels[col][pos] = symbol id
    final int[][] paylines;     // paylines[line][col] = row index

    // --- payouts (integer multipliers) ---
    /** linePayouts[symbolId][count] = line multiplier (over lineBet); 0 if that count does not pay. */
    final long[][] linePayouts;
    /** bestRegularByCount[count] = highest line multiplier among REGULAR symbols at that count. */
    final long[] bestRegularByCount;
    /** bestRegularSymbolByCount[count] = symbol id achieving bestRegularByCount[count]; -1 if none. */
    final int[] bestRegularSymbolByCount;
    /** scatterPayouts[symbolId][count] = "anywhere" multiplier (over betCents); 0 if it does not pay. */
    final long[][] scatterPayouts;
    /** Dense list of symbol ids that have scatterPays entries (iterated each spin). */
    final int[] scatterPaySymbols;

    // --- wild rule ---
    final boolean wildSubstitutesRegular;

    // --- free spins ---
    final boolean hasFreeSpins;
    final int fsTriggerSymbol;          // symbol id, or -1 if no free spins
    final int fsMinTriggerCount;
    final int[] fsAward;                // fsAward[scatterCount] = number of free spins granted
    final int fsMultiplier;
    final boolean fsRetrigger;

    CompiledGame(final long configId,
                 final int cols,
                 final int rows,
                 final String[] symbolIds,
                 final SymbolKind[] kinds,
                 final boolean[] wild,
                 final boolean[] scatter,
                 final int[][] reels,
                 final int[][] paylines,
                 final long[][] linePayouts,
                 final long[] bestRegularByCount,
                 final int[] bestRegularSymbolByCount,
                 final long[][] scatterPayouts,
                 final int[] scatterPaySymbols,
                 final boolean wildSubstitutesRegular,
                 final boolean hasFreeSpins,
                 final int fsTriggerSymbol,
                 final int fsMinTriggerCount,
                 final int[] fsAward,
                 final int fsMultiplier,
                 final boolean fsRetrigger) {
        this.configId = configId;
        this.cols = cols;
        this.rows = rows;
        this.symbolCount = symbolIds.length;
        this.paylineCount = paylines.length;
        this.maxSymbolCount = cols * rows;
        this.symbolIds = symbolIds;
        this.kinds = kinds;
        this.wild = wild;
        this.scatter = scatter;
        this.reels = reels;
        this.paylines = paylines;
        this.linePayouts = linePayouts;
        this.bestRegularByCount = bestRegularByCount;
        this.bestRegularSymbolByCount = bestRegularSymbolByCount;
        this.scatterPayouts = scatterPayouts;
        this.scatterPaySymbols = scatterPaySymbols;
        this.wildSubstitutesRegular = wildSubstitutesRegular;
        this.hasFreeSpins = hasFreeSpins;
        this.fsTriggerSymbol = fsTriggerSymbol;
        this.fsMinTriggerCount = fsMinTriggerCount;
        this.fsAward = fsAward;
        this.fsMultiplier = fsMultiplier;
        this.fsRetrigger = fsRetrigger;
    }

    /** @return the {@code game_configs.id} this game was compiled from. */
    public long configId() {
        return configId;
    }

    /** @return the number of columns / reels. */
    public int cols() {
        return cols;
    }

    /** @return the number of visible rows. */
    public int rows() {
        return rows;
    }

    /** @return the number of paylines (all active in the MVP). */
    public int paylineCount() {
        return paylineCount;
    }

    /** @return the number of distinct symbols. */
    public int symbolCount() {
        return symbolCount;
    }

    /**
     * Returns the original {@code config} id of a dense symbol id (for materialization/replay).
     *
     * @param symbolId dense symbol id ({@code 0..symbolCount-1})
     * @return the symbol's string id from the config
     */
    public String symbolId(final int symbolId) {
        return symbolIds[symbolId];
    }

    /**
     * Returns the kind of a dense symbol id.
     *
     * @param symbolId dense symbol id ({@code 0..symbolCount-1})
     * @return the {@link SymbolKind}
     */
    public SymbolKind kind(final int symbolId) {
        return kinds[symbolId];
    }

    /**
     * Number of free spins initially awarded for a given scatter count ({@code 0} if the game has no
     * free spins or the count does not trigger / is out of range).
     *
     * @param scatterCount number of trigger-symbol scatters
     * @return free spins awarded
     */
    public int freeSpinsAwardedFor(final int scatterCount) {
        if (!hasFreeSpins || scatterCount < 0 || scatterCount >= fsAward.length) {
            return 0;
        }
        return fsAward[scatterCount];
    }

    /** @return whether the game has a free-spins feature. */
    public boolean hasFreeSpins() {
        return hasFreeSpins;
    }

    /** @return the minimum scatter count that triggers free spins ({@code 0} if there are none). */
    public int freeSpinMinTriggerCount() {
        return fsMinTriggerCount;
    }

    /** @return the number of reels (columns). */
    public int reelCount() {
        return cols;
    }

    /** @return the length of a reel strip. */
    public int reelLength(final int reel) {
        return reels[reel].length;
    }

    /** @return the dense symbol id at a position of a reel strip. */
    public int reelSymbolAt(final int reel, final int position) {
        return reels[reel][position];
    }
}
