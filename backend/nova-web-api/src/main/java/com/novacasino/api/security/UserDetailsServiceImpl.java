package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.repository.OperatorJpaRepository;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads a user by email within the default operator (MVP is single-tenant).
 * Used by Spring Security for authentication and by the JWT filter.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String DEFAULT_OPERATOR = "novacasino-default";

    private final UserJpaRepository userRepo;
    private final OperatorJpaRepository operatorRepo;

    public UserDetailsServiceImpl(final UserJpaRepository userRepo,
                                  final OperatorJpaRepository operatorRepo) {
        this.userRepo     = userRepo;
        this.operatorRepo = operatorRepo;
    }

    @Override
    public UserDetails loadUserByUsername(final String email) throws UsernameNotFoundException {
        final Long operatorId = operatorRepo.findByCode(DEFAULT_OPERATOR)
                .orElseThrow(() -> new IllegalStateException("Default operator not found"))
                .getId();

        return userRepo.findByOperatorIdAndEmail(operatorId, email)
                .map(NovaUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }
}
