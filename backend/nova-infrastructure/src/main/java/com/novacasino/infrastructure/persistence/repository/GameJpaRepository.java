package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data repository for games. */
public interface GameJpaRepository extends JpaRepository<GameEntity, Long> {

    /** Active games ordered by id, for the player lobby. */
    List<GameEntity> findByActiveTrueOrderByIdAsc();

    /** An active game by id (inactive ones are treated as not found by players). */
    Optional<GameEntity> findByIdAndActiveTrue(Long id);

    /** All games of an operator (the mathematician sees active and inactive ones). */
    List<GameEntity> findByOperatorIdOrderByIdAsc(Long operatorId);
}
