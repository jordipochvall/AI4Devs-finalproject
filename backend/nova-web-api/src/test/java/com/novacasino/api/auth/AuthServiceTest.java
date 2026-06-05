package com.novacasino.api.auth;

import com.novacasino.api.auth.dto.AuthResponse;
import com.novacasino.api.auth.dto.LoginRequest;
import com.novacasino.api.auth.dto.RegisterRequest;
import com.novacasino.api.auth.exception.AgeVerificationException;
import com.novacasino.api.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.api.auth.exception.InvalidCredentialsException;
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

class AuthServiceTest {

    private UserJpaRepository     userRepo;
    private WalletJpaRepository   walletRepo;
    private OperatorJpaRepository operatorRepo;
    private JwtService            jwtService;
    private PasswordEncoder       passwordEncoder;
    private AuthService           authService;

    @BeforeEach
    void setUp() {
        userRepo        = mock(UserJpaRepository.class);
        walletRepo      = mock(WalletJpaRepository.class);
        operatorRepo    = mock(OperatorJpaRepository.class);
        jwtService      = mock(JwtService.class);
        passwordEncoder = new BCryptPasswordEncoder(4); // cost bajo para tests rápidos
        authService     = new AuthService(userRepo, walletRepo, operatorRepo, passwordEncoder, jwtService);

        when(operatorRepo.findByCode("novacasino-default")).thenReturn(Optional.of(stubOperator(1L)));
        when(jwtService.generateToken(any())).thenReturn("test-token");
        when(jwtService.getTtlSeconds()).thenReturn(3600L);
    }

    // --- AC1: menor de 18 → AgeVerificationException ---

    @Test
    void register_underAge_throws422() {
        RegisterRequest req = new RegisterRequest(
                "young@test.com", "Password1!", LocalDate.now().minusYears(17), "es");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AgeVerificationException.class);
        verifyNoInteractions(userRepo);
    }

    // --- AC3: límite exacto de edad (justo 18 pasa; un día menos falla) ---

    @Test
    void register_exactly18Today_succeeds() {
        // Nace hoy hace exactamente 18 años → cumple 18 hoy → permitido
        RegisterRequest req = new RegisterRequest(
                "edge18@test.com", "Password1!", LocalDate.now().minusYears(18), "es");

        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            setId(u, 1L);
            return u;
        });
        when(walletRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = authService.register(req);
        assertThat(resp.user().email()).isEqualTo("edge18@test.com");
    }

    @Test
    void register_oneDayBefore18_throws422() {
        // Cumple 18 mañana (le falta un día) → rechazado
        RegisterRequest req = new RegisterRequest(
                "edge17@test.com", "Password1!", LocalDate.now().minusYears(18).plusDays(1), "es");

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(AgeVerificationException.class);
        verifyNoInteractions(userRepo);
    }

    // --- AC2: registro válido crea usuario + wallet ---

    @Test
    void register_valid_createsUserAndWallet() {
        RegisterRequest req = new RegisterRequest(
                "adult@test.com", "Password1!", LocalDate.now().minusYears(25), "en");

        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(false);
        when(userRepo.save(any())).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            setId(u, 42L);
            return u;
        });
        when(walletRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse resp = authService.register(req);

        assertThat(resp.token()).isEqualTo("test-token");
        assertThat(resp.tokenType()).isEqualTo("Bearer");
        assertThat(resp.user().email()).isEqualTo("adult@test.com");
        assertThat(resp.user().role()).isEqualTo("PLAYER");
        verify(walletRepo).save(any(WalletEntity.class));
    }

    // --- AC2: email duplicado → EmailAlreadyRegisteredException ---

    @Test
    void register_duplicateEmail_throws409() {
        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest("dup@test.com", "Password1!", LocalDate.now().minusYears(20), "es")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    // --- AC3: login correcto → JWT ---

    @Test
    void login_validCredentials_returnsJwt() {
        UserEntity user = stubUser(1L, "user@test.com", passwordEncoder.encode("pass123"));
        when(userRepo.findByOperatorIdAndEmail(anyLong(), eq("user@test.com")))
                .thenReturn(Optional.of(user));

        AuthResponse resp = authService.login(new LoginRequest("user@test.com", "pass123"));

        assertThat(resp.token()).isEqualTo("test-token");
    }

    // --- AC3: credenciales incorrectas → InvalidCredentialsException ---

    @Test
    void login_wrongPassword_throws401() {
        UserEntity user = stubUser(1L, "user@test.com", passwordEncoder.encode("correct"));
        when(userRepo.findByOperatorIdAndEmail(anyLong(), eq("user@test.com")))
                .thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@test.com", "wrong")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    // --- AC6: el hash almacenado NO es la contraseña en claro ---

    @Test
    void register_passwordIsHashed() {
        RegisterRequest req = new RegisterRequest(
                "hash@test.com", "PlainPass1!", LocalDate.now().minusYears(30), "es");
        when(userRepo.existsByOperatorIdAndEmail(anyLong(), anyString())).thenReturn(false);

        UserEntity[] saved = new UserEntity[1];
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

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static OperatorEntity stubOperator(Long id) {
        OperatorEntity op = new OperatorEntity();
        setId(op, id);
        return op;
    }

    private static UserEntity stubUser(Long id, String email, String hash) {
        UserEntity u = new UserEntity();
        setId(u, id);
        u.setEmail(email);
        u.setPasswordHash(hash);
        u.setRole(UserRole.PLAYER);
        u.setBirthDate(LocalDate.now().minusYears(25));
        u.setLocale("es");
        return u;
    }

    private static <T> void setId(T entity, Long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
