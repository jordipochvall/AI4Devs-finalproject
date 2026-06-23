package com.novacasino.common.dto;

import java.math.BigDecimal;

/** Response to creating a version: id, version and declared targets. */
public record ConfigCreatedDto(
        Long       id,
        Long       gameId,
        int        version,
        BigDecimal rtpTarget,
        BigDecimal volatilityTarget
) {}
