package com.novacasino.api.player;

import com.fasterxml.jackson.databind.JsonNode;
import com.novacasino.domain.engine.GameConfigSpec;
import com.novacasino.domain.engine.GameConfigSpec.FreeSpinsSpec;
import com.novacasino.domain.engine.GameConfigSpec.PaytableEntry;
import com.novacasino.domain.engine.GameConfigSpec.SymbolSpec;
import com.novacasino.domain.engine.GameConfigSpec.WildSpec;
import com.novacasino.domain.engine.SymbolKind;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Maps the persisted {@code config} JSON tree (readme §3.3) into the engine's neutral
 * {@link GameConfigSpec}. This is the boundary the {@code nova-domain} engine deliberately stays
 * out of: it never sees Jackson. Assumes the config is already valid (it was validated by the
 * {@code ConfigValidator} at edit time, §3.3.3).
 */
@Component
public class GameConfigMapper {

    /**
     * Converts a config JSON tree into a {@link GameConfigSpec}.
     *
     * @param config the {@code game_configs.config} JSON tree
     * @return the neutral spec consumed by the {@code GameCompiler}
     */
    public GameConfigSpec toSpec(final JsonNode config) {
        final int cols = config.get("grid").get("cols").asInt();
        final int rows = config.get("grid").get("rows").asInt();
        final JsonNode bonus = config.get("bonus");

        return new GameConfigSpec(cols, rows,
                mapSymbols(config.get("symbols")),
                mapReels(config.get("reels")),
                mapPaylines(config.get("paylines")),
                mapPaytable(config.get("paytable")),
                mapScatterPays(config.get("scatterPays")),
                mapWild(bonus),
                mapFreeSpins(bonus));
    }

    private List<SymbolSpec> mapSymbols(final JsonNode symbolsNode) {
        final List<SymbolSpec> symbols = new ArrayList<>();
        for (final JsonNode s : symbolsNode) {
            symbols.add(new SymbolSpec(s.get("id").asText(), SymbolKind.valueOf(s.get("kind").asText())));
        }
        return symbols;
    }

    private List<List<String>> mapReels(final JsonNode reelsNode) {
        final List<List<String>> reels = new ArrayList<>();
        for (final JsonNode strip : reelsNode) {
            final List<String> compiledStrip = new ArrayList<>();
            for (final JsonNode sym : strip) {
                compiledStrip.add(sym.asText());
            }
            reels.add(compiledStrip);
        }
        return reels;
    }

    private List<List<Integer>> mapPaylines(final JsonNode paylinesNode) {
        final List<List<Integer>> paylines = new ArrayList<>();
        for (final JsonNode line : paylinesNode) {
            final List<Integer> compiledLine = new ArrayList<>();
            for (final JsonNode idx : line) {
                compiledLine.add(idx.asInt());
            }
            paylines.add(compiledLine);
        }
        return paylines;
    }

    private List<PaytableEntry> mapPaytable(final JsonNode paytableNode) {
        final List<PaytableEntry> paytable = new ArrayList<>();
        for (final JsonNode entry : paytableNode) {
            paytable.add(new PaytableEntry(entry.get("symbol").asText(), readCounts(entry.get("payouts"))));
        }
        return paytable;
    }

    private Map<String, Map<Integer, Long>> mapScatterPays(final JsonNode scatterPaysNode) {
        final Map<String, Map<Integer, Long>> scatterPays = new HashMap<>();
        if (scatterPaysNode != null && scatterPaysNode.isObject()) {
            final Iterator<String> fields = scatterPaysNode.fieldNames();
            while (fields.hasNext()) {
                final String sym = fields.next();
                scatterPays.put(sym, readCounts(scatterPaysNode.get(sym)));
            }
        }
        return scatterPays;
    }

    /** Optional wild substitution rules; null when the game has none. */
    private WildSpec mapWild(final JsonNode bonus) {
        if (bonus == null || !bonus.isObject()) {
            return null;
        }
        final JsonNode wildNode = bonus.get("wild");
        if (wildNode == null || !wildNode.isObject()) {
            return null;
        }
        return new WildSpec(readSubstitutes(wildNode.get("substitutes")));
    }

    /** Optional free-spins feature; null when the game has none. */
    private FreeSpinsSpec mapFreeSpins(final JsonNode bonus) {
        if (bonus == null || !bonus.isObject()) {
            return null;
        }
        final JsonNode fsNode = bonus.get("freeSpins");
        if (fsNode == null || !fsNode.isObject()) {
            return null;
        }
        return new FreeSpinsSpec(
                fsNode.get("triggerSymbol").asText(),
                fsNode.get("minTriggerCount").asInt(),
                readIntCounts(fsNode.get("award")),
                fsNode.get("multiplier").asInt(),
                fsNode.path("retrigger").asBoolean(false));
    }

    /** Reads a {@code {"<count>": multiplier}} map into {@code count -> long}. */
    private Map<Integer, Long> readCounts(final JsonNode payouts) {
        final Map<Integer, Long> map = new LinkedHashMap<>();
        final Iterator<String> keys = payouts.fieldNames();
        while (keys.hasNext()) {
            final String key = keys.next();
            map.put(Integer.parseInt(key), payouts.get(key).asLong());
        }
        return map;
    }

    /** Reads a {@code {"<count>": value}} map into {@code count -> int} (free-spins awards). */
    private Map<Integer, Integer> readIntCounts(final JsonNode award) {
        final Map<Integer, Integer> map = new LinkedHashMap<>();
        final Iterator<String> keys = award.fieldNames();
        while (keys.hasNext()) {
            final String key = keys.next();
            map.put(Integer.parseInt(key), award.get(key).asInt());
        }
        return map;
    }

    /** Reads the wild {@code substitutes} array into a set of {@link SymbolKind}. */
    private Set<SymbolKind> readSubstitutes(final JsonNode substitutes) {
        final Set<SymbolKind> kinds = java.util.EnumSet.noneOf(SymbolKind.class);
        if (substitutes != null && substitutes.isArray()) {
            for (final JsonNode kind : substitutes) {
                kinds.add(SymbolKind.valueOf(kind.asText()));
            }
        }
        return kinds;
    }
}
