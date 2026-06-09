package com.novacasino.domain.ai;

/**
 * Result of an AI explanation (HU-8).
 *
 * @param answer the natural-language answer
 * @param model  the model that produced it (e.g. {@code claude-haiku-4-5}); recorded for traceability
 */
public record Explanation(String answer, String model) {
}
