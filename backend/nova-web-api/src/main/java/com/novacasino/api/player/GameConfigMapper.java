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

        final List<SymbolSpec> symbols = new ArrayList<>();
        for (final JsonNode s : config.get("symbols")) {
            symbols.add(new SymbolSpec(s.get("id").asText(), SymbolKind.valueOf(s.get("kind").asText())));
        }

        final List<List<String>> reels = new ArrayList<>();
        for (final JsonNode strip : config.get("reels")) {
            final List<String> compiledStrip = new ArrayList<>();
            for (final JsonNode sym : strip) {
                compiledStrip.add(sym.asText());
            }
            reels.add(compiledStrip);
        }

        final List<List<Integer>> paylines = new ArrayList<>();
        for (final JsonNode line : config.get("paylines")) {
            final List<Integer> compiledLine = new ArrayList<>();
            for (final JsonNode idx : line) {
                compiledLine.add(idx.asInt());
            }
            paylines.add(compiledLine);
        }

        final List<PaytableEntry> paytable = new ArrayList<>();
        for (final JsonNode entry : config.get("paytable")) {
            paytable.add(new PaytableEntry(entry.get("symbol").asText(), readCounts(entry.get("payouts"))));
        }

        final Map<String, Map<Integer, Long>> scatterPays = new HashMap<>();
        final JsonNode scatterPaysNode = config.get("scatterPays");
        if (scatterPaysNode != null && scatterPaysNode.isObject()) {
            final Iterator<String> fields = scatterPaysNode.fieldNames();
            while (fields.hasNext()) {
                final String sym = fields.next();
                scatterPays.put(sym, readCounts(scatterPaysNode.get(sym)));
            }
        }

        WildSpec wild = null;
        FreeSpinsSpec freeSpins = null;
        final JsonNode bonus = config.get("bonus");
        if (bonus != null && bonus.isObject()) {
            final JsonNode wildNode = bonus.get("wild");
            if (wildNode != null && wildNode.isObject()) {
                wild = new WildSpec(readSubstitutes(wildNode.get("substitutes")));
            }
            final JsonNode fsNode = bonus.get("freeSpins");
            if (fsNode != null && fsNode.isObject()) {
                freeSpins = new FreeSpinsSpec(
                        fsNode.get("triggerSymbol").asText(),
                        fsNode.get("minTriggerCount").asInt(),
                        readIntCounts(fsNode.get("award")),
                        fsNode.get("multiplier").asInt(),
                        fsNode.path("retrigger").asBoolean(false));
            }
        }

        return new GameConfigSpec(cols, rows, symbols, reels, paylines, paytable, scatterPays, wild, freeSpins);
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
