package com.novacasino.api.auth;

import com.novacasino.api.auth.dto.AuthResponse;
import com.novacasino.api.auth.dto.LoginRequest;
import com.novacasino.api.auth.dto.LogoutRequest;
import com.novacasino.api.auth.dto.RefreshRequest;
import com.novacasino.api.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Public authentication endpoints (register and login). */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/v1/auth/register — registers a player and returns a JWT (auto-login). */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody final RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(req));
    }

    /** POST /api/v1/auth/login — authenticates any role and returns a JWT. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody final LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    /** POST /api/v1/auth/refresh — renews the session from a valid refresh token (HU-13). */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody final RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.refreshToken()));
    }

    /** POST /api/v1/auth/logout — revokes a refresh token (HU-13). */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody final LogoutRequest req) {
        authService.logout(req.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
