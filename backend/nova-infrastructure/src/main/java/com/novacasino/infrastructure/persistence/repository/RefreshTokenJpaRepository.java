package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data repository for refresh tokens (HU-13). */
public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

    /** Looks up a token by the SHA-256 hash of its opaque value. */
    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);
}
