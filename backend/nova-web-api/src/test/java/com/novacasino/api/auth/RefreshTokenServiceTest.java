package com.novacasino.api.auth;

import com.novacasino.api.auth.exception.InvalidRefreshTokenException;
import com.novacasino.infrastructure.persistence.entity.RefreshTokenEntity;
import com.novacasino.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for {@link RefreshTokenService}: hashing, rotation and revocation. */
class RefreshTokenServiceTest {

    private RefreshTokenJpaRepository repo;
    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        repo = mock(RefreshTokenJpaRepository.class);
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));
        service = new RefreshTokenService(repo, 3600L);
    }

    @Test
    void issue_persistsHashedTokenAndReturnsOpaqueValue() {
        final String token = service.issue(1L);

        final ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(repo).save(captor.capture());
        final RefreshTokenEntity saved = captor.getValue();
        assertThat(token).isNotBlank();
        // The opaque token is never stored: only its SHA-256 (64 hex chars), different from the token.
        assertThat(saved.getTokenHash()).hasSize(64).isNotEqualTo(token);
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getExpiresAt()).isAfter(OffsetDateTime.now());
    }

    @Test
    void consume_validToken_revokesAndReturnsUserId() {
        // Issue a token and capture what was stored so we can look it up by its hash.
        final String token = service.issue(7L);
        final ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(repo).save(captor.capture());
        final RefreshTokenEntity stored = captor.getValue();
        when(repo.findByTokenHash(stored.getTokenHash())).thenReturn(Optional.of(stored));

        final Long userId = service.consume(token);

        assertThat(userId).isEqualTo(7L);
        assertThat(stored.isRevoked()).isTrue(); // rotation: the consumed token is revoked
    }

    @Test
    void consume_unknownToken_throws() {
        when(repo.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.consume("nope"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void consume_revokedOrExpiredToken_throws() {
        final RefreshTokenEntity revoked = new RefreshTokenEntity(1L, "h", OffsetDateTime.now().plusDays(1));
        revoked.setRevoked(true);
        when(repo.findByTokenHash(any())).thenReturn(Optional.of(revoked));
        assertThatThrownBy(() -> service.consume("x")).isInstanceOf(InvalidRefreshTokenException.class);

        final RefreshTokenEntity expired = new RefreshTokenEntity(1L, "h", OffsetDateTime.now().minusSeconds(1));
        when(repo.findByTokenHash(any())).thenReturn(Optional.of(expired));
        assertThatThrownBy(() -> service.consume("y")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void revoke_marksTokenRevoked_andIsNoOpWhenUnknown() {
        final RefreshTokenEntity entity = new RefreshTokenEntity(1L, "h", OffsetDateTime.now().plusDays(1));
        when(repo.findByTokenHash(any())).thenReturn(Optional.of(entity));
        service.revoke("token");
        assertThat(entity.isRevoked()).isTrue();

        when(repo.findByTokenHash(any())).thenReturn(Optional.empty());
        service.revoke("unknown"); // must not throw
    }
}
