package com.novacasino.api.math.dto;

/** Game with its active math version, for the math backoffice. */
public record MathGameDto(
        Long    id,
        String  code,
        String  name,
        String  theme,
        boolean active,
        Long    activeConfigId,
        Integer activeVersion
) {}
