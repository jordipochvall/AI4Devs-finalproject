package com.novacasino.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

/** Detail of a math version: full config plus the declared targets. */
public record ConfigDetailDto(
        Long       id,
        Long       gameId,
        int        version,
        BigDecimal rtpTarget,
        BigDecimal volatilityTarget,
        String     notes,
        JsonNode   config
) {}
