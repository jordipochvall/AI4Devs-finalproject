package com.novacasino.domain.engine;

import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compiles a {@link GameConfigSpec} into an immutable {@link CompiledGame} of primitive structures,
 * and caches the result by {@code configId} (readme §2.1.7). Game configs are immutable, so a
 * compiled game is reused forever across spins and across the production / simulator paths.
 *
 * <p>The compiler assumes the spec is already valid (it is validated at edit time by the
 * {@code ConfigValidator}, readme §3.3.3); it performs the translation, not the validation.
 *
 * <p>Thread-safe: the cache is a {@link ConcurrentHashMap} and the produced {@link CompiledGame} is
 * immutable and read-only.
 */
public final class GameCompiler {

    private final Map<Long, CompiledGame> cache = new ConcurrentHashMap<>();

    /**
     * Returns the {@link CompiledGame} for the given config, compiling and caching it on first use.
     *
     * @param configId the {@code game_configs.id} (cache key)
     * @param spec     the validated config spec
     * @return the cached, immutable compiled game
     */
    public CompiledGame compile(final long configId, final GameConfigSpec spec) {
        return cache.computeIfAbsent(configId, id -> build(id, spec));
    }

    /** Performs the actual spec → primitives translation. */
    private CompiledGame build(final long configId, final GameConfigSpec spec) {
        final int cols = spec.cols();
        final int rows = spec.rows();
        final int maxSymbolCount = cols * rows;

        // --- symbols → dense int ids, in spec order ---
        final List<SymbolSpec> symbolSpecs = spec.symbols();
        final int symbolCount = symbolSpecs.size();
        final String[] symbolIds = new String[symbolCount];
        final SymbolKind[] kinds = new SymbolKind[symbolCount];
        final boolean[] wild = new boolean[symbolCount];
        final boolean[] scatter = new boolean[symbolCount];
        final Map<String, Integer> index = new HashMap<>(symbolCount * 2);
        for (int i = 0; i < symbolCount; i++) {
            final SymbolSpec s = symbolSpecs.get(i);
            symbolIds[i] = s.id();
            kinds[i] = s.kind();
            wild[i] = s.kind() == SymbolKind.WILD;
            scatter[i] = s.kind() == SymbolKind.SCATTER;
            index.put(s.id(), i);
        }

        // --- reels → int[col][pos] ---
        final List<List<String>> reelSpec = spec.reels();
        final int[][] reels = new int[cols][];
        for (int c = 0; c < cols; c++) {
            final List<String> strip = reelSpec.get(c);
            final int[] compiledStrip = new int[strip.size()];
            for (int p = 0; p < compiledStrip.length; p++) {
                compiledStrip[p] = index.get(strip.get(p));
            }
            reels[c] = compiledStrip;
        }

        // --- paylines → int[line][col] = row ---
        final List<List<Integer>> paylineSpec = spec.paylines();
        final int[][] paylines = new int[paylineSpec.size()][cols];
        for (int l = 0; l < paylines.length; l++) {
            final List<Integer> line = paylineSpec.get(l);
            for (int c = 0; c < cols; c++) {
                paylines[l][c] = line.get(c);
            }
        }

        // --- line payouts (REGULAR) + best-regular-by-count ---
        final long[][] linePayouts = new long[symbolCount][cols + 1];
        for (final PaytableEntry entry : spec.paytable()) {
            final int sym = index.get(entry.symbol());
            for (final Map.Entry<Integer, Long> p : entry.payouts().entrySet()) {
                final int count = p.getKey();
                if (count >= 0 && count <= cols) {
                    linePayouts[sym][count] = p.getValue();
                }
            }
        }
        final long[] bestRegularByCount = new long[cols + 1];
        final int[] bestRegularSymbolByCount = new int[cols + 1];
        for (int count = 0; count <= cols; count++) {
            long best = 0L;
            int bestSym = -1;
            for (int sym = 0; sym < symbolCount; sym++) {
                if (kinds[sym] == SymbolKind.REGULAR && linePayouts[sym][count] > best) {
                    best = linePayouts[sym][count];
                    bestSym = sym;
                }
            }
            bestRegularByCount[count] = best;
            bestRegularSymbolByCount[count] = bestSym;
        }

        // --- scatter payouts (anywhere, by total count) ---
        final long[][] scatterPayouts = new long[symbolCount][maxSymbolCount + 1];
        final List<Integer> scatterPaySymbolList = new ArrayList<>();
        final Map<String, Map<Integer, Long>> scatterPays = spec.scatterPays();
        if (scatterPays != null) {
            for (final Map.Entry<String, Map<Integer, Long>> e : scatterPays.entrySet()) {
                final int sym = index.get(e.getKey());
                scatterPaySymbolList.add(sym);
                for (final Map.Entry<Integer, Long> p : e.getValue().entrySet()) {
                    final int count = p.getKey();
                    if (count >= 0 && count <= maxSymbolCount) {
                        scatterPayouts[sym][count] = p.getValue();
                    }
                }
            }
        }
        final int[] scatterPaySymbols = scatterPaySymbolList.stream().mapToInt(Integer::intValue).toArray();

        // --- free spins ---
        final GameConfigSpec.FreeSpinsSpec fs = spec.freeSpins();
        final boolean hasFreeSpins = fs != null;
        final int fsTriggerSymbol = hasFreeSpins ? index.get(fs.triggerSymbol()) : -1;
        final int fsMinTriggerCount = hasFreeSpins ? fs.minTriggerCount() : 0;
        final int[] fsAward = new int[maxSymbolCount + 1];
        int fsMultiplier = 1;
        boolean fsRetrigger = false;
        if (hasFreeSpins) {
            for (final Map.Entry<Integer, Integer> a : fs.award().entrySet()) {
                final int count = a.getKey();
                if (count >= 0 && count <= maxSymbolCount) {
                    fsAward[count] = a.getValue();
                }
            }
            fsMultiplier = fs.multiplier();
            fsRetrigger = fs.retrigger();
        }

        // --- wild rule ---
        final boolean wildSubstitutesRegular =
                spec.wild() != null && spec.wild().substitutes().contains(SymbolKind.REGULAR);

        return new CompiledGame(
                configId, cols, rows,
                symbolIds, kinds, wild, scatter,
                reels, paylines,
                linePayouts, bestRegularByCount, bestRegularSymbolByCount,
                scatterPayouts, scatterPaySymbols,
                wildSubstitutesRegular,
                hasFreeSpins, fsTriggerSymbol, fsMinTriggerCount, fsAward, fsMultiplier, fsRetrigger);
    }
}
