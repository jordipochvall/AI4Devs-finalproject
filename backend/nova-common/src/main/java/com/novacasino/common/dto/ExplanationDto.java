package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/**
 * Answer to an AI explanation request (HU-8 §4.2).
 *
 * @param question the question asked
 * @param answer   the AI answer
 * @param model    the model used (traceability)
 * @param askedAt  when it was asked/answered
 */
public record ExplanationDto(String question, String answer, String model, OffsetDateTime askedAt) {
}
