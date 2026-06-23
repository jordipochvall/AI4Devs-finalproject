package com.novacasino.api.auth;

import com.novacasino.application.auth.PasswordHasherPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adapter for {@link PasswordHasherPort} backed by the Spring Security {@link PasswordEncoder}. Lives
 * in the web module where the encoder bean is configured, keeping {@code nova-application} free of any
 * Spring Security dependency.
 */
@Component
public class PasswordHasherAdapter implements PasswordHasherPort {

    private final PasswordEncoder passwordEncoder;

    public PasswordHasherAdapter(final PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(final String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
