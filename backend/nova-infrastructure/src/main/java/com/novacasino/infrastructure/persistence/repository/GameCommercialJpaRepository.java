package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameCommercialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Commercial-configuration view of games, scoped to an operator (HU-15). */
public interface GameCommercialJpaRepository extends JpaRepository<GameCommercialEntity, Long> {

    /** All games of an operator, ordered by id. */
    List<GameCommercialEntity> findByOperatorIdOrderByIdAsc(Long operatorId);

    /** A game by id constrained to the operator (operator isolation, AC5). */
    Optional<GameCommercialEntity> findByIdAndOperatorId(Long id, Long operatorId);
}
