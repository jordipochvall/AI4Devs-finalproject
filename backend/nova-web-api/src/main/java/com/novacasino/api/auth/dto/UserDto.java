package com.novacasino.api.auth.dto;

/** Public view of a user returned in auth responses. */
public record UserDto(
        Long   id,
        String email,
        String role,
        String locale
) {}
