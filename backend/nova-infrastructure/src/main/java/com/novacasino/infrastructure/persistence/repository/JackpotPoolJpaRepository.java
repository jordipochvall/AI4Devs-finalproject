package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.JackpotPoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Progressive jackpot pool per game (HU-26). */
public interface JackpotPoolJpaRepository extends JpaRepository<JackpotPoolEntity, Long> {
    Optional<JackpotPoolEntity> findByGameId(Long gameId);
}
