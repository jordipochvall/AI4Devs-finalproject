package com.novacasino.api.player.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** Resumen de un juego para el lobby (sin exponer la matemática completa). */
public record GameSummaryDto(
        Long     id,
        String   name,
        String   theme,
        String   coverImageUrl,
        JsonNode grid
) {}
