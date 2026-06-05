package com.novacasino.api.auth;

import com.novacasino.api.auth.dto.AuthResponse;
import com.novacasino.api.auth.dto.LoginRequest;
import com.novacasino.api.auth.dto.RegisterRequest;
import com.novacasino.api.auth.dto.UserDto;
import com.novacasino.api.auth.exception.AgeVerificationException;
import com.novacasino.api.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.api.auth.exception.InvalidCredentialsException;
import com.novacasino.api.security.JwtService;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.repository.OperatorJpaRepository;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

/**
 * Casos de uso de autenticación: registro (+ wallet) y login.
 * En MVP single-tenant resuelve siempre al operador 'novacasino-default'.
 */
@Service
public class AuthService {

    private static final String DEFAULT_OPERATOR_CODE = "novacasino-default";

    private final UserJpaRepository     userRepo;
    private final WalletJpaRepository   walletRepo;
    private final OperatorJpaRepository operatorRepo;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;

    public AuthService(UserJpaRepository userRepo,
                       WalletJpaRepository walletRepo,
                       OperatorJpaRepository operatorRepo,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepo        = userRepo;
        this.walletRepo      = walletRepo;
        this.operatorRepo    = operatorRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    /** Registra un jugador y crea su wallet (saldo 0) en la misma transacción. */
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        // AC1: verificar mayoría de edad (DGOJ)
        if (Period.between(req.birthDate(), LocalDate.now()).getYears() < 18) {
            throw new AgeVerificationException();
        }

        Long operatorId = defaultOperatorId();

        // 409 si el email ya existe en este operador
        if (userRepo.existsByOperatorIdAndEmail(operatorId, req.email())) {
            throw new EmailAlreadyRegisteredException(req.email());
        }

        // Crear usuario — password hasheado con BCrypt cost 12 (AC6)
        UserEntity user = new UserEntity();
        user.setOperatorId(operatorId);
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(UserRole.PLAYER);
        user.setBirthDate(req.birthDate());
        user.setLocale(req.locale() != null && !req.locale().isBlank() ? req.locale() : "es");
        user = userRepo.save(user);

        // Crear wallet con saldo 0 (AC2 implícito; §3.2.3)
        WalletEntity wallet = new WalletEntity();
        wallet.setOperatorId(operatorId);
        wallet.setUserId(user.getId());
        wallet.setBalanceCents(0L);
        walletRepo.save(wallet);

        String token = jwtService.generateToken(user);
        return toAuthResponse(token, user);
    }

    /** Autentica cualquier rol (jugador, operador, matemático). */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Long operatorId = defaultOperatorId();

        UserEntity user = userRepo.findByOperatorIdAndEmail(operatorId, req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        return toAuthResponse(token, user);
    }

    // -------------------------------------------------------------------------

    private Long defaultOperatorId() {
        return operatorRepo.findByCode(DEFAULT_OPERATOR_CODE)
                .orElseThrow(() -> new IllegalStateException("Operador por defecto no encontrado"))
                .getId();
    }

    private AuthResponse toAuthResponse(String token, UserEntity user) {
        UserDto userDto = new UserDto(user.getId(), user.getEmail(),
                user.getRole().name(), user.getLocale());
        return new AuthResponse(token, jwtService.getTtlSeconds(), userDto);
    }
}
