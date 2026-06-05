package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByOperatorIdAndEmail(Long operatorId, String email);

    boolean existsByOperatorIdAndEmail(Long operatorId, String email);
}
