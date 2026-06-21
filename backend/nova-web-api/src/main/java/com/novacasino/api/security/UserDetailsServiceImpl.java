package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a user by email across operators (multi-tenant since HU-25; emails are unique platform-wide).
 * Used by Spring Security for authentication and by the JWT filter.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserJpaRepository userRepo;

    public UserDetailsServiceImpl(final UserJpaRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email)
                .map(NovaUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
