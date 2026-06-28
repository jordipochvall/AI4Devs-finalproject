package com.novacasino.application.math.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.novacasino.application.math.validation.ConfigValidationException.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Validates the business invariants of the `config` (readme §3.3.3) before persisting a version.
 * Accumulates all errors and throws {@link ConfigValidationException} if there is any.
 *
 * <p>Covers: references to existing symbols, reel/payline dimensions, correct kind in
 * paytable/scatterPays/triggerSymbol and gap-free payout coverage (contiguous counts).
 */
public class ConfigValidator {

    /** Validates the given config, throwing {@link ConfigValidationException} on any error. */
    public void validate(final JsonNode config) {
        final List<FieldError> errors = new ArrayList<>();
        if (config == null || !config.isObject()) {
            errors.add(new FieldError("config", "config must be a JSON object"));
            throw new ConfigValidationException(errors);
        }

        final int[] dims = validateGrid(config, errors);             // [cols, rows]
        final Map<String, String> symbols = validateSymbols(config, errors);
        validateReels(config, dims[0], symbols, errors);
        validatePaylines(config, dims[0], dims[1], errors);
        validatePaytable(config, symbols, errors);
        validateScatterPays(config, symbols, errors);
        validateBonus(config, symbols, errors);

        if (!errors.isEmpty()) {
            throw new ConfigValidationException(errors);
        }
    }

    /** Validates {@code grid} and returns its [cols, rows] (0,0 when absent/invalid). */
    private int[] validateGrid(final JsonNode config, final List<FieldError> errors) {
        final JsonNode grid = config.get("grid");
        if (grid == null || !grid.hasNonNull("cols") || !grid.hasNonNull("rows")) {
            errors.add(new FieldError("grid", "grid must have cols and rows"));
            return new int[]{0, 0};
        }
        final int cols = grid.get("cols").asInt();
        final int rows = grid.get("rows").asInt();
        if (cols <= 0) {
            errors.add(new FieldError("grid.cols", "cols must be > 0"));
        }
        if (rows <= 0) {
            errors.add(new FieldError("grid.rows", "rows must be > 0"));
        }
        return new int[]{cols, rows};
    }

    /** Validates {@code symbols} and returns the id→kind map (used to cross-check the rest). */
    private Map<String, String> validateSymbols(final JsonNode config, final List<FieldError> errors) {
        final Map<String, String> symbols = new HashMap<>();
        final JsonNode symbolsNode = config.get("symbols");
        if (symbolsNode == null || !symbolsNode.isArray() || symbolsNode.isEmpty()) {
            errors.add(new FieldError("symbols", "symbols must be a non-empty array"));
            return symbols;
        }
        for (int i = 0; i < symbolsNode.size(); i++) {
            final JsonNode s = symbolsNode.get(i);
            final String id = s.path("id").asText(null);
            final String kind = s.path("kind").asText(null);
            if (id == null) {
                errors.add(new FieldError("symbols[" + i + "].id", "id is required"));
                continue;
            }
            if (kind == null || !List.of("REGULAR", "WILD", "SCATTER").contains(kind)) {
                errors.add(new FieldError("symbols[" + i + "].kind",
                        "kind must be REGULAR, WILD or SCATTER"));
            }
            if (symbols.put(id, kind) != null) {
                errors.add(new FieldError("symbols", "duplicate id: " + id));
            }
        }
        return symbols;
    }

    /** Validates {@code reels}: exactly {@code cols} non-empty strips of known symbols. */
    private void validateReels(final JsonNode config, final int cols,
                               final Map<String, String> symbols, final List<FieldError> errors) {
        final JsonNode reels = config.get("reels");
        if (reels == null || !reels.isArray()) {
            errors.add(new FieldError("reels", "reels must be an array"));
            return;
        }
        if (cols > 0 && reels.size() != cols) {
            errors.add(new FieldError("reels", "there must be exactly " + cols + " reel strips"));
        }
        for (int r = 0; r < reels.size(); r++) {
            final JsonNode strip = reels.get(r);
            if (!strip.isArray() || strip.isEmpty()) {
                errors.add(new FieldError("reels[" + r + "]", "each strip must be a non-empty array"));
                continue;
            }
            for (final JsonNode sym : strip) {
                final String id = sym.asText();
                if (!symbols.containsKey(id)) {
                    errors.add(new FieldError("reels[" + r + "]", "unknown symbol: " + id));
                }
            }
        }
    }

    /** Validates {@code paylines}: non-empty, each with {@code cols} in-range row indices. */
    private void validatePaylines(final JsonNode config, final int cols, final int rows,
                                  final List<FieldError> errors) {
        final JsonNode paylines = config.get("paylines");
        if (paylines == null || !paylines.isArray() || paylines.isEmpty()) {
            errors.add(new FieldError("paylines", "paylines must be a non-empty array"));
            return;
        }
        for (int p = 0; p < paylines.size(); p++) {
            final JsonNode line = paylines.get(p);
            if (!line.isArray() || (cols > 0 && line.size() != cols)) {
                errors.add(new FieldError("paylines[" + p + "]",
                        "each payline must have " + cols + " indices"));
                continue;
            }
            for (final JsonNode idx : line) {
                final int row = idx.asInt(-1);
                if (rows > 0 && (row < 0 || row >= rows)) {
                    errors.add(new FieldError("paylines[" + p + "]",
                            "row index out of range: " + row));
                }
            }
        }
    }

    /** Validates {@code paytable}: REGULAR symbols only with gap-free contiguous payouts. */
    private void validatePaytable(final JsonNode config, final Map<String, String> symbols,
                                  final List<FieldError> errors) {
        final JsonNode paytable = config.get("paytable");
        if (paytable == null || !paytable.isArray()) {
            errors.add(new FieldError("paytable", "paytable must be an array"));
            return;
        }
        for (int i = 0; i < paytable.size(); i++) {
            final JsonNode entry = paytable.get(i);
            final String sym = entry.path("symbol").asText(null);
            final String field = "paytable[" + i + "]";
            if (sym == null || !symbols.containsKey(sym)) {
                errors.add(new FieldError(field + ".symbol", "unknown symbol: " + sym));
            } else if (!"REGULAR".equals(symbols.get(sym))) {
                errors.add(new FieldError(field + ".symbol", "paytable only accepts REGULAR symbols: " + sym));
            }
            checkContiguousCounts(entry.get("payouts"), field + ".payouts", errors);
        }
    }

    /** Validates the optional {@code scatterPays}: SCATTER symbols only with contiguous payouts. */
    private void validateScatterPays(final JsonNode config, final Map<String, String> symbols,
                                     final List<FieldError> errors) {
        final JsonNode scatterPays = config.get("scatterPays");
        if (scatterPays == null || !scatterPays.isObject()) {
            return;
        }
        final Iterator<String> it = scatterPays.fieldNames();
        while (it.hasNext()) {
            final String sym = it.next();
            final String field = "scatterPays." + sym;
            if (!symbols.containsKey(sym)) {
                errors.add(new FieldError(field, "unknown symbol: " + sym));
            } else if (!"SCATTER".equals(symbols.get(sym))) {
                errors.add(new FieldError(field, "scatterPays only accepts SCATTER symbols: " + sym));
            }
            checkContiguousCounts(scatterPays.get(sym), field, errors);
        }
    }

    /** Validates the optional {@code bonus.freeSpins}: a SCATTER trigger and a contiguous award table. */
    private void validateBonus(final JsonNode config, final Map<String, String> symbols,
                               final List<FieldError> errors) {
        final JsonNode bonus = config.get("bonus");
        if (bonus == null || !bonus.hasNonNull("freeSpins")) {
            return;
        }
        final JsonNode fs = bonus.get("freeSpins");
        final String trigger = fs.path("triggerSymbol").asText(null);
        if (trigger == null || !symbols.containsKey(trigger)) {
            errors.add(new FieldError("bonus.freeSpins.triggerSymbol", "unknown symbol: " + trigger));
        } else if (!"SCATTER".equals(symbols.get(trigger))) {
            errors.add(new FieldError("bonus.freeSpins.triggerSymbol",
                    "triggerSymbol must be of kind SCATTER"));
        }
        checkContiguousCounts(fs.get("award"), "bonus.freeSpins.award", errors);
    }

    /**
     * Checks that the count keys of a payout map are contiguous integers (no gaps between the
     * declared minimum and maximum) — gap-free coverage (§3.3.3).
     */
    private void checkContiguousCounts(final JsonNode payouts, final String field,
                                       final List<FieldError> errors) {
        if (payouts == null || !payouts.isObject() || payouts.isEmpty()) {
            errors.add(new FieldError(field, "must declare at least one count with its value"));
            return;
        }
        final TreeSet<Integer> counts = new TreeSet<>();
        final Iterator<String> keys = payouts.fieldNames();
        while (keys.hasNext()) {
            final String k = keys.next();
            try {
                counts.add(Integer.parseInt(k));
            } catch (final NumberFormatException e) {
                errors.add(new FieldError(field, "non-numeric count key: " + k));
            }
        }
        if (!counts.isEmpty()) {
            for (int n = counts.first(); n <= counts.last(); n++) {
                if (!counts.contains(n)) {
                    errors.add(new FieldError(field, "payout gap: missing count " + n));
                }
            }
        }
    }
}
