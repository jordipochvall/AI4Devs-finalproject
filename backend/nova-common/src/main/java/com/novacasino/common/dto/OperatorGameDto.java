package com.novacasino.common.dto;

import java.util.List;

/** A game's commercial configuration as seen by the operator backoffice (HU-15). */
public record OperatorGameDto(
        Long id,
        String code,
        String name,
        String theme,
        long minBetCents,
        long maxBetCents,
        long betStepCents,
        List<String> allowedCurrencies,
        boolean active,
        Long activeConfigId,
        Integer paylineCount
) {
}
