package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.GameConfigPublicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data repository for the append-only math publication history (HU-17). */
public interface GameConfigPublicationJpaRepository
        extends JpaRepository<GameConfigPublicationEntity, Long> {
}
