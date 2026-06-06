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
 * Authentication use cases: registration (with wallet) and login.
 * In the single-tenant MVP it always resolves to the 'novacasino-default' operator.
 */
@Service
public class AuthService {

    private static final String DEFAULT_OPERATOR_CODE = "novacasino-default";

    private final UserJpaRepository     userRepo;
    private final WalletJpaRepository   walletRepo;
    private final OperatorJpaRepository operatorRepo;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;

    public AuthService(final UserJpaRepository userRepo,
                       final WalletJpaRepository walletRepo,
                       final OperatorJpaRepository operatorRepo,
                       final PasswordEncoder passwordEncoder,
                       final JwtService jwtService) {
        this.userRepo        = userRepo;
        this.walletRepo      = walletRepo;
        this.operatorRepo    = operatorRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService      = jwtService;
    }

    /** Registers a player and creates their wallet (zero balance) in the same transaction. */
    @Transactional
    public AuthResponse register(final RegisterRequest req) {
        // AC1: enforce minimum age (DGOJ)
        if (Period.between(req.birthDate(), LocalDate.now()).getYears() < 18) {
            throw new AgeVerificationException();
        }

        final Long operatorId = defaultOperatorId();

        // 409 if the email already exists within this operator
        if (userRepo.existsByOperatorIdAndEmail(operatorId, req.email())) {
            throw new EmailAlreadyRegisteredException(req.email());
        }

        // Create the user — password hashed with BCrypt cost 12 (AC6)
        UserEntity user = new UserEntity();
        user.setOperatorId(operatorId);
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setRole(UserRole.PLAYER);
        user.setBirthDate(req.birthDate());
        user.setLocale(req.locale() != null && !req.locale().isBlank() ? req.locale() : "es");
        user = userRepo.save(user);

        // Create the wallet with zero balance (§3.2.3)
        final WalletEntity wallet = new WalletEntity();
        wallet.setOperatorId(operatorId);
        wallet.setUserId(user.getId());
        wallet.setBalanceCents(0L);
        walletRepo.save(wallet);

        final String token = jwtService.generateToken(user);
        return toAuthResponse(token, user);
    }

    /** Authenticates any role (player, operator, math analyst). */
    @Transactional(readOnly = true)
    public AuthResponse login(final LoginRequest req) {
        final Long operatorId = defaultOperatorId();

        final UserEntity user = userRepo.findByOperatorIdAndEmail(operatorId, req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        final String token = jwtService.generateToken(user);
        return toAuthResponse(token, user);
    }

    // -------------------------------------------------------------------------

    private Long defaultOperatorId() {
        return operatorRepo.findByCode(DEFAULT_OPERATOR_CODE)
                .orElseThrow(() -> new IllegalStateException("Default operator not found"))
                .getId();
    }

    private AuthResponse toAuthResponse(final String token, final UserEntity user) {
        final UserDto userDto = new UserDto(user.getId(), user.getEmail(),
                user.getRole().name(), user.getLocale());
        return new AuthResponse(token, jwtService.getTtlSeconds(), userDto);
    }
}
