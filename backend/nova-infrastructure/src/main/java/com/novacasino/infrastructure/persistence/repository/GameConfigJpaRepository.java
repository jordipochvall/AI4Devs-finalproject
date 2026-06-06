package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data repository for game math configurations. */
public interface GameConfigJpaRepository extends JpaRepository<GameConfigEntity, Long> {

    /** Latest math version of a game, used to compute the next version number. */
    Optional<GameConfigEntity> findTopByGameIdOrderByVersionDesc(Long gameId);
}
