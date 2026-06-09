package com.novacasino.domain.engine;

/**
 * Visitor that receives the outcome of every spin (base spin and each free spin) resolved by the
 * {@link SpinKernel}. It is the single seam of the "data-oriented core + double materialization"
 * design (readme §2.1.7): the kernel decides, the sink chooses <em>what</em> to do with the result.
 *
 * <ul>
 *   <li>{@code CountingSink} (simulator) aggregates into counters and discards — zero materialization.</li>
 *   <li>{@code MaterializingSink} (production) copies the result into rich domain objects — one spin at a time.</li>
 * </ul>
 *
 * <p><strong>Buffers are reused.</strong> All arrays passed to {@link #onSpin} are owned by the
 * kernel and overwritten on the next spin; an implementation that needs to retain them must copy.
 * The four {@code winningLine*} arrays are parallel: index {@code i} of each describes the same
 * winning line, for {@code i} in {@code [0, winningLineCount)}.
 */
public interface RoundSink {

    /**
     * Called once per resolved spin.
     *
     * @param window             visible symbols as dense symbol ids, <strong>column-major</strong>:
     *                           {@code window[col * rows + row]}. Reused buffer — copy to retain.
     * @param cols               number of columns in {@code window}
     * @param rows               number of rows in {@code window}
     * @param winningLines       payline indices (into the config's paylines) that won
     * @param winningLineSymbols anchor symbol id of each winning line (parallel to {@code winningLines})
     * @param winningLineCounts  matched symbol count of each winning line (parallel)
     * @param winningLineWins    prize in cents of each winning line (parallel, multiplier applied)
     * @param winningLineCount   number of valid entries in the parallel {@code winningLine*} arrays
     * @param winCents           total prize of this spin in cents (line + scatter, multiplier applied)
     * @param freeSpin           {@code true} if this is a free spin (no wager), {@code false} for the base spin
     * @param multiplier         prize multiplier in effect for this spin ({@code 1} on the base spin)
     * @param scatterCount       number of trigger-symbol scatters present in {@code window}
     */
    void onSpin(int[] window,
                int cols,
                int rows,
                int[] winningLines,
                int[] winningLineSymbols,
                int[] winningLineCounts,
                long[] winningLineWins,
                int winningLineCount,
                long winCents,
                boolean freeSpin,
                int multiplier,
                int scatterCount);
}
