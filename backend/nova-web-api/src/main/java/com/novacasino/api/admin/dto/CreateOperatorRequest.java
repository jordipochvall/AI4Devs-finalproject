package com.novacasino.api.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to onboard a new operator together with its initial operator user (HU-25).
 *
 * @param code            stable external code (unique)
 * @param name            display name
 * @param operatorEmail   email of the initial OPERATOR user
 * @param operatorPassword password of the initial OPERATOR user
 */
public record CreateOperatorRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email String operatorEmail,
        @NotBlank @Size(min = 8, max = 100) String operatorPassword) {
}
