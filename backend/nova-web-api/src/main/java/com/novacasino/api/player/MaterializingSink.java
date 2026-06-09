package com.novacasino.api.player;

import com.novacasino.api.player.dto.WinningPaylineDto;
import com.novacasino.domain.engine.CompiledGame;
import com.novacasino.domain.engine.RoundSink;

import java.util.ArrayList;
import java.util.List;

/**
 * Production-side {@link RoundSink}: the counterpart of the simulator's {@code CountingSink}
 * (readme §2.1.7). Same kernel, different materialization — it copies each spin's reused buffers
 * into immutable domain-shaped objects ({@link MechSpin}), one spin at a time. Not thread-safe; one
 * instance per spin round.
 */
final class MaterializingSink implements RoundSink {

    private final CompiledGame game;
    private final List<MechSpin> spins = new ArrayList<>();

    MaterializingSink(final CompiledGame game) {
        this.game = game;
    }

    @Override
    public void onSpin(final int[] window, final int cols, final int rows,
                       final int[] winningLines, final int[] winningLineSymbols,
                       final int[] winningLineCounts, final long[] winningLineWins,
                       final int winningLineCount, final long winCents, final boolean freeSpin,
                       final int multiplier, final int scatterCount) {
        // view: one sub-list per column (window is column-major: window[col * rows + row]).
        final List<List<String>> view = new ArrayList<>(cols);
        for (int c = 0; c < cols; c++) {
            final List<String> column = new ArrayList<>(rows);
            for (int r = 0; r < rows; r++) {
                column.add(game.symbolId(window[c * rows + r]));
            }
            view.add(column);
        }

        final List<WinningPaylineDto> winningPaylines = new ArrayList<>(winningLineCount);
        for (int i = 0; i < winningLineCount; i++) {
            winningPaylines.add(new WinningPaylineDto(
                    winningLines[i], game.symbolId(winningLineSymbols[i]),
                    winningLineCounts[i], winningLineWins[i]));
        }

        spins.add(new MechSpin(view, winningPaylines, scatterCount, multiplier, winCents, freeSpin));
    }

    /** The resolved spins in order: index 0 is the base spin, the rest are free spins. */
    List<MechSpin> spins() {
        return spins;
    }

    /**
     * Immutable mechanical outcome of one spin (no money/balance — those are the service's concern).
     *
     * @param view            visible symbols, one sub-list per column
     * @param winningPaylines winning paylines
     * @param scatterCount    trigger-symbol scatters present
     * @param multiplier      prize multiplier in effect ({@code 1} on the base spin)
     * @param winCents        this spin's prize in cents (multiplier applied)
     * @param freeSpin        whether this is a free spin
     */
    record MechSpin(List<List<String>> view, List<WinningPaylineDto> winningPaylines,
                    int scatterCount, int multiplier, long winCents, boolean freeSpin) {
    }
}
