package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameJpaRepository extends JpaRepository<GameEntity, Long> {

    List<GameEntity> findByActiveTrueOrderByIdAsc();

    Optional<GameEntity> findByIdAndActiveTrue(Long id);
}
