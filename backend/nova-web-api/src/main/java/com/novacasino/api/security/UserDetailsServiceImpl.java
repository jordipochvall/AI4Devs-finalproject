package com.novacasino.api.security;

import com.novacasino.infrastructure.persistence.repository.OperatorJpaRepository;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carga un usuario por email dentro del operador por defecto (MVP single-tenant).
 * Spring Security usa este servicio para autenticar y para el filtro JWT.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final String DEFAULT_OPERATOR = "novacasino-default";

    private final UserJpaRepository userRepo;
    private final OperatorJpaRepository operatorRepo;

    public UserDetailsServiceImpl(UserJpaRepository userRepo, OperatorJpaRepository operatorRepo) {
        this.userRepo     = userRepo;
        this.operatorRepo = operatorRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Long operatorId = operatorRepo.findByCode(DEFAULT_OPERATOR)
                .orElseThrow(() -> new IllegalStateException("Operador por defecto no encontrado"))
                .getId();

        return userRepo.findByOperatorIdAndEmail(operatorId, email)
                .map(NovaUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
    }
}
