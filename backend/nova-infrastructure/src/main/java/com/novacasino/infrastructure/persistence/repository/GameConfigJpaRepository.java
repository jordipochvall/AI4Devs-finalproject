package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameConfigJpaRepository extends JpaRepository<GameConfigEntity, Long> {
}
