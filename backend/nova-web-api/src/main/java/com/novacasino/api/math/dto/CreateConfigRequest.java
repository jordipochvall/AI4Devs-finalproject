package com.novacasino.api.math.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Body for creating a math version. The mathematician <b>declares</b> the target
 * RTP/volatility (the platform does not compute them).
 */
public record CreateConfigRequest(
        @NotNull JsonNode config,
        @NotNull BigDecimal rtpTarget,
        BigDecimal volatilityTarget,
        String notes
) {}
