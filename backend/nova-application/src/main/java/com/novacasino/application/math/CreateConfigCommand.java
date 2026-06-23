package com.novacasino.application.math;

import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;

/** Command to create a new math version (HU-17): the declared config and targets. */
public record CreateConfigCommand(
        JsonNode config,
        BigDecimal rtpTarget,
        BigDecimal volatilityTarget,
        String notes) {
}
