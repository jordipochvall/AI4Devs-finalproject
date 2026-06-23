package com.novacasino.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** Lobby summary of a game (without exposing the full math). */
public record GameSummaryDto(
        Long     id,
        String   name,
        String   theme,
        String   coverImageUrl,
        JsonNode grid
) {}
