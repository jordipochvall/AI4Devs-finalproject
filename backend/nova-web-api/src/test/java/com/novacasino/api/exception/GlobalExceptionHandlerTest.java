package com.novacasino.api.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the GlobalExceptionHandler.
 * Verifies AC1 (en), AC2 (es/default) and AC4 (invariant structure).
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setDefaultLocale(Locale.forLanguageTag("es"));
        handler = new GlobalExceptionHandler(source);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    // --- AC2: Spanish by default ---

    @Test
    void internalError_defaultLocale_returnsSpanish() {
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        MockHttpServletRequest req = new MockHttpServletRequest();

        ResponseEntity<ProblemDetail> response =
                handler.handleAll(new RuntimeException("boom"), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Error interno del servidor");
        assertThat(body.getDetail()).isEqualTo("Se ha producido un error inesperado. Por favor, inténtalo de nuevo más tarde.");
    }

    // --- AC1: Accept-Language: en → English ---

    @Test
    void internalError_englishLocale_returnsEnglish() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        MockHttpServletRequest req = new MockHttpServletRequest();

        ResponseEntity<ProblemDetail> response =
                handler.handleAll(new RuntimeException("boom"), req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ProblemDetail body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTitle()).isEqualTo("Internal Server Error");
        assertThat(body.getDetail()).isEqualTo("An unexpected error occurred. Please try again later.");
    }

    // --- AC4: language does not change the HTTP status nor the structure ---

    @Test
    void structureIsIdenticalRegardlessOfLocale() {
        MockHttpServletRequest req = new MockHttpServletRequest();

        LocaleContextHolder.setLocale(Locale.forLanguageTag("es"));
        ResponseEntity<ProblemDetail> es = handler.handleAll(new RuntimeException(), req);
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        ResponseEntity<ProblemDetail> en = handler.handleAll(new RuntimeException(), req);

        // same HTTP status
        assertThat(es.getStatusCode()).isEqualTo(en.getStatusCode());
        // both have type, title and detail
        assertThat(es.getBody()).isNotNull();
        assertThat(en.getBody()).isNotNull();
        assertThat(es.getBody().getType()).isEqualTo(en.getBody().getType());
        assertThat(es.getBody().getStatus()).isEqualTo(en.getBody().getStatus());
    }

    // --- AC3: all keys exist in both bundles ---

    @Test
    void allRequiredKeysExistInBothLocales() {
        ResourceBundleMessageSource src = new ResourceBundleMessageSource();
        src.setBasenames("i18n/messages");
        src.setDefaultEncoding("UTF-8");

        String[] keys = {
                "error.badRequest.title",        "error.badRequest.detail",
                "error.unauthorized.title",      "error.unauthorized.detail",
                "error.forbidden.title",         "error.forbidden.detail",
                "error.notFound.title",          "error.notFound.detail",
                "error.internalError.title",     "error.internalError.detail",
                "error.validation.title",        "error.validation.detail",
                "error.rateLimitExceeded.title", "error.rateLimitExceeded.detail",
                "error.auth.invalidCredentials.detail",
                "error.auth.emailAlreadyRegistered.detail",
                "error.auth.ageVerificationFailed.detail",
                "error.spin.betOutOfRange.detail",
                "error.spin.insufficientBalance.detail",
                "error.spin.betNotMultipleOfLines.detail",
                "error.idempotency.conflict.detail",
                "error.idempotency.concurrentModification.detail"
        };

        for (String key : keys) {
            String es = src.getMessage(key, new Object[]{0, 0}, Locale.forLanguageTag("es"));
            String en = src.getMessage(key, new Object[]{0, 0}, Locale.ENGLISH);
            assertThat(es).as("Missing ES key: %s", key).isNotEqualTo(key);
            assertThat(en).as("Missing EN key: %s", key).isNotEqualTo(key);
        }
    }
}
