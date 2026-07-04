package com.novacasino.api.math.ai;

import com.novacasino.domain.ai.Explainer;
import com.novacasino.domain.ai.Explanation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline (no external AI) {@link Explainer}: active by default when {@code anthropic.enabled} is not
 * {@code true}. Instead of a 503, it returns a <strong>deterministic heuristic</strong> summary built
 * from the simulation metrics embedded in the prompt (RTP and its split, hit frequency, volatility,
 * max win, free-spins trigger). This keeps "ask the AI" functional without an API key (HU-32); the
 * conversational Claude path activates when {@code anthropic.enabled=true} + a key is configured.
 */
@Component
@ConditionalOnProperty(name = "anthropic.enabled", havingValue = "false", matchIfMissing = true)
public class OfflineExplainer implements Explainer {

    /** Model tag persisted for traceability so offline answers are distinguishable from Claude's. */
    static final String MODEL = "offline-heuristic";

    private final Locale es = Locale.forLanguageTag("es");

    @Override
    public Explanation explain(final String prompt) {
        final Double rtp = num(prompt, "RTP empirical:");
        final Double base = num(prompt, "RTP base game:");
        final Double free = num(prompt, "RTP free spins:");
        final Double hit = num(prompt, "hit frequency:");
        final Double vol = num(prompt, "volatility:");
        final Double maxWin = num(prompt, "max win multiplier:");
        final Double trigger = num(prompt, "free-spins trigger freq:");

        final StringBuilder sb = new StringBuilder("[Análisis local — sin IA externa]\n");
        if (rtp != null) {
            sb.append("RTP empírico: ").append(pct(rtp));
            if (base != null && free != null) {
                sb.append(" (base ").append(pct(base)).append(" + tiradas gratis ").append(pct(free)).append(')');
            }
            sb.append(" — el juego devuelve ~").append(Math.round(rtp * 100))
              .append(" c€ por cada 1 € apostado.\n");
        }
        if (hit != null && hit > 0) {
            sb.append("Frecuencia de aciertos: ").append(pct(hit))
              .append(" (≈1 premio de cada ").append(Math.max(1, Math.round(1.0 / hit))).append(" giros).\n");
        }
        if (vol != null) {
            sb.append("Volatilidad: ").append(String.format(es, "%.1f", vol))
              .append(" (").append(volLabel(vol)).append(").\n");
        }
        if (maxWin != null) {
            sb.append("Máximo premio observado: ").append(String.format(es, "%.0f×", maxWin)).append(" la apuesta");
            if (trigger != null && trigger > 0) {
                sb.append("; las tiradas gratis se activan en el ").append(pct(trigger)).append(" de los giros");
            }
            sb.append(".\n");
        }
        sb.append("Nota: resumen heurístico de las métricas medidas. Para respuestas conversacionales, ")
          .append("activa la IA (ANTHROPIC_ENABLED=true y una API key de Anthropic).");

        return new Explanation(sb.toString(), MODEL);
    }

    /** Extracts the first number following {@code label} in the prompt, or {@code null}. */
    private static Double num(final String prompt, final String label) {
        final Matcher m = Pattern.compile(Pattern.quote(label) + "\\s*([-0-9]+(?:\\.[0-9]+)?)").matcher(prompt);
        try {
            return m.find() ? Double.valueOf(m.group(1)) : null;
        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private String pct(final double fraction) {
        return String.format(es, "%.1f%%", fraction * 100);
    }

    private static String volLabel(final double vol) {
        if (vol < 3.0) return "baja";
        if (vol < 8.0) return "media";
        return "alta";
    }
}
