package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.PlayerLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Responsible-gaming limits per player (HU-19). */
public interface PlayerLimitJpaRepository extends JpaRepository<PlayerLimitEntity, Long> {

    Optional<PlayerLimitEntity> findByUserIdAndLimitTypeAndPeriod(Long userId, String limitType, String period);

    List<PlayerLimitEntity> findByUserId(Long userId);
}
