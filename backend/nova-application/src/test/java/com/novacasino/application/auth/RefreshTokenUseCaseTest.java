package com.novacasino.application.auth;

import com.novacasino.application.auth.exception.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-13 refresh-token rotation/revocation in {@link RefreshTokenUseCase} (port mocked). */
class RefreshTokenUseCaseTest {

    private RefreshTokenStorePort store;
    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        store = mock(RefreshTokenStorePort.class);
        useCase = new RefreshTokenUseCase(store, 3600L);
    }

    @Test
    void issue_persistsHashedTokenAndReturnsOpaqueValue() {
        final String token = useCase.issue(7L);

        final ArgumentCaptor<String> hash = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<OffsetDateTime> exp = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(store).save(eq(7L), hash.capture(), exp.capture());
        assertThat(token).isNotBlank();
        assertThat(hash.getValue()).hasSize(64).isNotEqualTo(token); // SHA-256 hex, not the token
        assertThat(exp.getValue()).isAfter(OffsetDateTime.now());
    }

    @Test
    void consume_validToken_revokesAndReturnsUserId() {
        when(store.findByHash(any()))
                .thenReturn(Optional.of(new StoredRefreshToken(5L, 7L, OffsetDateTime.now().plusDays(1), false)));

        final Long userId = useCase.consume("opaque");

        assertThat(userId).isEqualTo(7L);
        verify(store).markRevoked(5L); // rotation
    }

    @Test
    void consume_unknownToken_throws() {
        when(store.findByHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.consume("nope")).isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void consume_revokedOrExpiredToken_throws() {
        when(store.findByHash(any()))
                .thenReturn(Optional.of(new StoredRefreshToken(5L, 7L, OffsetDateTime.now().plusDays(1), true)));
        assertThatThrownBy(() -> useCase.consume("x")).isInstanceOf(InvalidRefreshTokenException.class);

        when(store.findByHash(any()))
                .thenReturn(Optional.of(new StoredRefreshToken(5L, 7L, OffsetDateTime.now().minusSeconds(1), false)));
        assertThatThrownBy(() -> useCase.consume("y")).isInstanceOf(InvalidRefreshTokenException.class);
        verify(store, never()).markRevoked(anyLong());
    }

    @Test
    void revoke_marksTokenRevoked_andIsNoOpWhenUnknown() {
        when(store.findByHash(any()))
                .thenReturn(Optional.of(new StoredRefreshToken(5L, 7L, OffsetDateTime.now().plusDays(1), false)));
        useCase.revoke("token");
        verify(store).markRevoked(5L);

        when(store.findByHash(any())).thenReturn(Optional.empty());
        useCase.revoke("unknown"); // must not throw
    }
}
