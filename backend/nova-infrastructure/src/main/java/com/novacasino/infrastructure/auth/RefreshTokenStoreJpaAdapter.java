package com.novacasino.infrastructure.auth;

import com.novacasino.application.auth.RefreshTokenStorePort;
import com.novacasino.application.auth.StoredRefreshToken;
import com.novacasino.infrastructure.persistence.entity.RefreshTokenEntity;
import com.novacasino.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

/** JPA adapter for {@link RefreshTokenStorePort} (HU-13). */
@Component
public class RefreshTokenStoreJpaAdapter implements RefreshTokenStorePort {

    private final RefreshTokenJpaRepository repo;

    public RefreshTokenStoreJpaAdapter(final RefreshTokenJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void save(final Long userId, final String tokenHash, final OffsetDateTime expiresAt) {
        repo.save(new RefreshTokenEntity(userId, tokenHash, expiresAt));
    }

    @Override
    public Optional<StoredRefreshToken> findByHash(final String tokenHash) {
        return repo.findByTokenHash(tokenHash)
                .map(e -> new StoredRefreshToken(e.getId(), e.getUserId(), e.getExpiresAt(), e.isRevoked()));
    }

    @Override
    public void markRevoked(final Long id) {
        repo.findById(id).ifPresent(e -> { e.setRevoked(true); repo.save(e); });
    }
}
