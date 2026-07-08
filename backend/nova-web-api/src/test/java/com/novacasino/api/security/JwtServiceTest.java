package com.novacasino.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** HU-36 — the app must refuse to start with a weak/example {@code app.jwt.secret}. */
class JwtServiceTest {

    private JwtService serviceWithSecret(final String secret) {
        final JwtService service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", secret);
        return service;
    }

    @Test
    void rejectsSecretShorterThanTheMinimum() {
        assertThatThrownBy(() -> serviceWithSecret("admin").validateSecret())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> serviceWithSecret(null).validateSecret())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void acceptsSecretAtLeastTheMinimumLength() {
        final String strongSecret = "a".repeat(JwtService.MIN_SECRET_BYTES);
        serviceWithSecret(strongSecret).validateSecret(); // must not throw
    }
}
