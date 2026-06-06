package com.novacasino.api.math.validation;

import java.util.List;

/** Mathematically invalid config — 422 with per-field detail in {@code errors[]}. */
public class ConfigValidationException extends RuntimeException {

    /** Validation error for a specific config field. */
    public record FieldError(String field, String message) {}

    private final transient List<FieldError> errors;

    public ConfigValidationException(final List<FieldError> errors) {
        super("The math configuration has validation errors");
        this.errors = errors;
    }

    public List<FieldError> getErrors() {
        return errors;
    }
}
