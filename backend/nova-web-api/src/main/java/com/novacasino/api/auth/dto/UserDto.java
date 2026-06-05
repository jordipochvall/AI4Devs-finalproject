package com.novacasino.api.auth.dto;

public record UserDto(
        Long   id,
        String email,
        String role,
        String locale
) {}
