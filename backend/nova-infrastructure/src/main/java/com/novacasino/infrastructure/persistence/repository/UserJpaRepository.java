package com.novacasino.infrastructure.persistence.repository;

import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data repository for users. */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /** Finds a user by email within an operator (the unique key in multi-tenant mode). */
    Optional<UserEntity> findByOperatorIdAndEmail(Long operatorId, String email);

    /** Finds a user by email across operators (login; emails are unique platform-wide, HU-25). */
    Optional<UserEntity> findByEmail(String email);

    /** Returns whether an email already exists within an operator. */
    boolean existsByOperatorIdAndEmail(Long operatorId, String email);

    /** Paginated search of players by partial, case-insensitive email. */
    Page<UserEntity> findByOperatorIdAndRoleAndEmailContainingIgnoreCase(
            Long operatorId, UserRole role, String email, Pageable pageable);
}
