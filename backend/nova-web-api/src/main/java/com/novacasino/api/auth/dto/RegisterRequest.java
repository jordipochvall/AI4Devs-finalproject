package com.novacasino.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @NotNull
        LocalDate birthDate,

        String locale   // opcional; defecto "es"
) {}
