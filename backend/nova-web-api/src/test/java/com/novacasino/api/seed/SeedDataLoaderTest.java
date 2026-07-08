package com.novacasino.api.seed;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * HU-38 — seed account passwords come from configuration (SEED_*_PASSWORD), not hardcoded literals,
 * so a public demo can set its own values while still seeding the same demo accounts.
 */
class SeedDataLoaderTest {

    @Test
    void insertsUsersWithTheConfiguredPasswordsNotTheDevDefaults() {
        final JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        final AtomicReference<String> capturedAdminHash = new AtomicReference<>();
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    // Mockito flattens the varargs: [sql, requiredType, operatorId, email, hash, role, ...]
                    final Object[] raw = invocation.getArguments();
                    final String sql = String.valueOf(raw[0]);
                    if (sql.contains("INSERT INTO users") && raw.length > 4 && "admin@nova.test".equals(raw[3])) {
                        capturedAdminHash.set(String.valueOf(raw[4]));
                    }
                    return 1L;
                });

        final SeedDataLoader loader = new SeedDataLoader(
                jdbc, "custom-admin-pw", "operator123", "math123", "player123");
        loader.run(new DefaultApplicationArguments());

        assertThat(capturedAdminHash.get()).isNotNull();
        assertThat(new BCryptPasswordEncoder(12).matches("custom-admin-pw", capturedAdminHash.get())).isTrue();
        assertThat(new BCryptPasswordEncoder(12).matches("admin123", capturedAdminHash.get())).isFalse();
    }

    @Test
    void blankOverrideFallsBackToTheDevDefault_notAnEmptyPassword() {
        final JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        final AtomicReference<String> capturedAdminHash = new AtomicReference<>();
        when(jdbc.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenAnswer(invocation -> {
                    final Object[] raw = invocation.getArguments();
                    final String sql = String.valueOf(raw[0]);
                    if (sql.contains("INSERT INTO users") && raw.length > 4 && "admin@nova.test".equals(raw[3])) {
                        capturedAdminHash.set(String.valueOf(raw[4]));
                    }
                    return 1L;
                });

        // A container env var explicitly set to "" (e.g. an unfilled .env.example line) must not
        // result in an empty password.
        final SeedDataLoader loader = new SeedDataLoader(jdbc, "", "operator123", "math123", "player123");
        loader.run(new DefaultApplicationArguments());

        assertThat(new BCryptPasswordEncoder(12).matches("admin123", capturedAdminHash.get())).isTrue();
    }
}
