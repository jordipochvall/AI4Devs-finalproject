package com.novacasino.api.auth;

import com.novacasino.api.auth.dto.AuthResponse;
import com.novacasino.api.auth.dto.LoginRequest;
import com.novacasino.api.auth.dto.RegisterRequest;
import com.novacasino.api.auth.dto.UserDto;
import com.novacasino.api.auth.exception.AgeVerificationException;
import com.novacasino.api.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.api.auth.exception.InvalidCredentialsException;
import com.novacasino.api.auth.exception.InvalidRefreshTokenException;
import com.novacasino.api.auth.exception.OperatorInactiveException;
import com.novacasino.api.security.JwtService;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
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
    private final RefreshTokenService   refreshTokenService;

    public AuthService(final UserJpaRepository userRepo,
                       final WalletJpaRepository walletRepo,
                       final OperatorJpaRepository operatorRepo,
                       final PasswordEncoder passwordEncoder,
                       final JwtService jwtService,
                       final RefreshTokenService refreshTokenService) {
        this.userRepo            = userRepo;
        this.walletRepo          = walletRepo;
        this.operatorRepo        = operatorRepo;
        this.passwordEncoder     = passwordEncoder;
        this.jwtService          = jwtService;
        this.refreshTokenService = refreshTokenService;
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

        return issueSession(user);
    }

    /**
     * Authenticates any role (player, operator, math analyst, admin) and starts a session. Resolves
     * the user by email across operators (multi-tenant, HU-25) and refuses login when the user or its
     * operator is inactive (AC3 — a deactivated operator's users cannot operate).
     */
    @Transactional
    public AuthResponse login(final LoginRequest req) {
        final UserEntity user = userRepo.findByEmail(req.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        final OperatorEntity operator = operatorRepo.findById(user.getOperatorId())
                .orElseThrow(InvalidCredentialsException::new);
        if (!operator.isActive() || !user.isActive()) {
            throw new OperatorInactiveException();
        }

        return issueSession(user);
    }

    /**
     * Renews the session from a valid refresh token (HU-13). Rotates the refresh token (the presented
     * one is revoked and a new one is issued) and mints a fresh access token.
     */
    @Transactional
    public AuthResponse refresh(final String refreshToken) {
        final Long userId = refreshTokenService.consume(refreshToken);
        final UserEntity user = userRepo.findById(userId)
                .orElseThrow(InvalidRefreshTokenException::new);
        return issueSession(user);
    }

    /** Revokes a refresh token on logout (best-effort; idempotent). */
    @Transactional
    public void logout(final String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    // -------------------------------------------------------------------------

    private Long defaultOperatorId() {
        return operatorRepo.findByCode(DEFAULT_OPERATOR_CODE)
                .orElseThrow(() -> new IllegalStateException("Default operator not found"))
                .getId();
    }

    /** Mints an access token and a fresh refresh token for the user. */
    private AuthResponse issueSession(final UserEntity user) {
        final String accessToken = jwtService.generateToken(user);
        final String refreshToken = refreshTokenService.issue(user.getId());
        final UserDto userDto = new UserDto(user.getId(), user.getEmail(),
                user.getRole().name(), user.getLocale());
        return new AuthResponse(accessToken, jwtService.getTtlSeconds(), refreshToken, userDto);
    }
}
