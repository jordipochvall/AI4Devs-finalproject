package com.novacasino.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Login request (any role). */
public record LoginRequest(
        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {}
