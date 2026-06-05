package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OperatorJpaRepository extends JpaRepository<OperatorEntity, Long> {

    Optional<OperatorEntity> findByCode(String code);
}
