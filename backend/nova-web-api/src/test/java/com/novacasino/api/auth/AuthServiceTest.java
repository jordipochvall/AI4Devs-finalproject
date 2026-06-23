package com.novacasino.api.auth;

import com.novacasino.api.auth.dto.AuthResponse;
import com.novacasino.api.auth.dto.LoginRequest;
import com.novacasino.api.auth.dto.RegisterRequest;
import com.novacasino.api.auth.exception.AgeVerificationException;
import com.novacasino.application.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.api.auth.exception.InvalidCredentialsException;
import com.novacasino.application.auth.exception.InvalidRefreshTokenException;
import com.novacasino.application.auth.RefreshTokenUseCase;
import com.novacasino.api.security.JwtService;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import com.novacasino.infrastructure.persistence.entity.WalletEntity;
import com.novacasino.infrastructure.persistence.repository.OperatorJpaRepository;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import com.novacasino.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Unit tests for {@link AuthService} (registration and login) with mocked repositories. */
class AuthServiceTest {

    private UserJpaRepository     userRepo;
    private WalletJpaRepository   walletRepo;
    private OperatorJpaRepository operatorRepo;
    private JwtService            jwtService;
    private RefreshTokenUseCase   refreshTokenService;
    private PasswordEncoder       passwordEncoder;
    private AuthService           authService;

    @BeforeEach
    void setUp() {
        userRepo            = mock(UserJpaRepository.class);
        walletRepo          = mock(WalletJpaRepository.class);
        operatorRepo        = mock(OperatorJpaRepository.class);
        jwtService          = mock(JwtService.class);
        refreshTokenService = mock(RefreshTokenUseCase.class);
        passwordEncoder     = new BCryptPasswordEncoder(4); // low cost for fast tests
        authService         = new AuthService(userRepo, walletRepo, operatorRepo, passwordEncoder,
                jwtService, refreshTokenService);

        when(operatorRepo.findByCode("novacasino-default")).thenReturn(Optional.of(stubOperator(1L)));
        when(operatorRepo.findById(1L)).thenReturn(Optional.of(stubOperator(1L))); // active by default
        when(jwtService.generateToken(any())).thenReturn("test-token");
        when(jwtService.getTtlSeconds()).thenReturn(3600L);
        when(refreshTokenService.issue(anyLong())).thenReturn("test-refresh");
    }

    // --- AC1: under 18 → AgeVerificationException ---

    @Test
    void register_underAge_throws422() {
        final RegisterRequest req = new RegisterRequest(
                "young@test.com", "Password1!", LocalDate.now().minusYears(17), "es");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AgeVerificationException.class);
        verifyNoInteractions(userRepo);
    }

    // --- AC3: exact age boundary (exactly 18 passes; one day short fails) ---

    @Test
    void register_exactly18Today_succeeds() {
        // Born exactly 18 years ago today → turns 18 today → allowed
        final RegisterRequest req = new RegisterRequest(
                "edge18@test.com", "Password1!", LocalDate.now().minusYears(18), "es");

        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> {
            final UserEntity u = inv.getArgument(0);
            setId(u, 1L);
            return u;
        });
        when(walletRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final AuthResponse resp = authService.register(req);
        assertThat(resp.user().email()).isEqualTo("edge18@test.com");
    }

    @Test
    void register_oneDayBefore18_throws422() {
        // Turns 18 tomorrow (one day short) → rejected
        final RegisterRequest req = new RegisterRequest(
                "edge17@test.com", "Password1!", LocalDate.now().minusYears(18).plusDays(1), "es");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AgeVerificationException.class);
        verifyNoInteractions(userRepo);
    }

    // --- AC2: valid registration creates user + wallet ---

    @Test
    void register_valid_createsUserAndWallet() {
        final RegisterRequest req = new RegisterRequest(
                "adult@test.com", "Password1!", LocalDate.now().minusYears(25), "en");

        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> {
            final UserEntity u = inv.getArgument(0);
            setId(u, 42L);
            return u;
        });
        when(walletRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        final AuthResponse resp = authService.register(req);

        assertThat(resp.token()).isEqualTo("test-token");
        assertThat(resp.tokenType()).isEqualTo("Bearer");
        assertThat(resp.user().email()).isEqualTo("adult@test.com");
        assertThat(resp.user().role()).isEqualTo("PLAYER");
        verify(walletRepo).save(any(WalletEntity.class));
    }

    // --- AC2: duplicate email → EmailAlreadyRegisteredException ---

    @Test
    void register_duplicateEmail_throws409() {
        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("dup@test.com", "Password1!", LocalDate.now().minusYears(20), "es")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    // --- AC3: correct login → JWT ---

    @Test
    void login_validCredentials_returnsJwt() {
        final UserEntity user = stubUser(1L, "user@test.com", passwordEncoder.encode("pass123"));
        when(userRepo.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(user));

        final AuthResponse resp = authService.login(new LoginRequest("user@test.com", "pass123"));

        assertThat(resp.token()).isEqualTo("test-token");
    }

    // --- AC3: wrong credentials → InvalidCredentialsException ---

    @Test
    void login_wrongPassword_throws401() {
        final UserEntity user = stubUser(1L, "user@test.com", passwordEncoder.encode("correct"));
        when(userRepo.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // --- AC6: the stored hash is NOT the plaintext password ---

    @Test
    void register_passwordIsHashed() {
        final RegisterRequest req = new RegisterRequest(
                "hash@test.com", "PlainPass1!", LocalDate.now().minusYears(30), "es");
        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(false);

        final UserEntity[] saved = new UserEntity[1];
        when(userRepo.save(any())).thenAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            setId(saved[0], 99L);
            return saved[0];
        });
        when(walletRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        authService.register(req);

        assertThat(saved[0].getPasswordHash()).isNotEqualTo("PlainPass1!");
        assertThat(passwordEncoder.matches("PlainPass1!", saved[0].getPasswordHash())).isTrue();
    }

    // --- HU-13: login/register issue a refresh token ---

    @Test
    void login_issuesRefreshToken() {
        final UserEntity user = stubUser(1L, "user@test.com", passwordEncoder.encode("pass123"));
        when(userRepo.findByEmail(eq("user@test.com")))
                .thenReturn(Optional.of(user));

        final AuthResponse resp = authService.login(new LoginRequest("user@test.com", "pass123"));

        assertThat(resp.refreshToken()).isEqualTo("test-refresh");
        verify(refreshTokenService).issue(1L);
    }

    // --- HU-13: refresh rotates and mints a new session ---

    @Test
    void refresh_validToken_returnsNewSession() {
        final UserEntity user = stubUser(7L, "user@test.com", "hash");
        when(refreshTokenService.consume("old-refresh")).thenReturn(7L);
        when(userRepo.findById(7L)).thenReturn(Optional.of(user));

        final AuthResponse resp = authService.refresh("old-refresh");

        assertThat(resp.token()).isEqualTo("test-token");
        assertThat(resp.refreshToken()).isEqualTo("test-refresh");
        verify(refreshTokenService).consume("old-refresh"); // the old one is rotated/revoked
        verify(refreshTokenService).issue(7L);
    }

    @Test
    void refresh_userGone_throws() {
        when(refreshTokenService.consume("orphan")).thenReturn(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("orphan"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logout_revokesRefreshToken() {
        authService.logout("some-refresh");
        verify(refreshTokenService).revoke("some-refresh");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static OperatorEntity stubOperator(final Long id) {
        final OperatorEntity op = new OperatorEntity();
        setId(op, id);
        return op;
    }

    private static UserEntity stubUser(final Long id, final String email, final String hash) {
        final UserEntity u = new UserEntity();
        setId(u, id);
        u.setOperatorId(1L);
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setRole(UserRole.PLAYER);
        u.setBirthDate(LocalDate.now().minusYears(25));
        u.setLocale("es");
        return u;
    }

    /** Sets the private {@code id} field by reflection (entities expose no id setter). */
    private static <T> void setId(final T entity, final Long id) {
        try {
            final var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
