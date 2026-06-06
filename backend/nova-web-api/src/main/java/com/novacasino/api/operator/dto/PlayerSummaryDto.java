package com.novacasino.api.operator.dto;

/** Player with their balance, for the operator backoffice listing. */
public record PlayerSummaryDto(
        Long    id,
        String  email,
        String  locale,
        boolean active,
        Long    balanceCents,
        String  currency
) {}
