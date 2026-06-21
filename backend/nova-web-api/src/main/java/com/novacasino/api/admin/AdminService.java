package com.novacasino.api.admin;

import com.novacasino.api.admin.dto.CreateOperatorRequest;
import com.novacasino.api.admin.dto.OperatorDto;
import com.novacasino.api.admin.exception.OperatorCodeExistsException;
import com.novacasino.api.admin.exception.OperatorNotFoundException;
import com.novacasino.api.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import com.novacasino.infrastructure.persistence.repository.OperatorJpaRepository;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Platform-admin use cases for multi-tenant management (HU-25): list operators and onboard a new
 * operator together with its initial OPERATOR user. New operators start active and isolated; their
 * users authenticate by email across tenants (see AuthService.login).
 */
@Service
public class AdminService {

    private final OperatorJpaRepository operatorRepo;
    private final UserJpaRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminService(final OperatorJpaRepository operatorRepo, final UserJpaRepository userRepo,
                        final PasswordEncoder passwordEncoder) {
        this.operatorRepo    = operatorRepo;
        this.userRepo        = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /** Lists all operators (AC: admin overview). */
    @Transactional(readOnly = true)
    public List<OperatorDto> listOperators() {
        return operatorRepo.findAllByOrderByIdAsc().stream().map(AdminService::toDto).toList();
    }

    /**
     * Creates an operator and its initial OPERATOR user (AC1). The user can then log in over empty,
     * isolated data (AC2).
     *
     * @throws OperatorCodeExistsException      if the code is taken (409)
     * @throws EmailAlreadyRegisteredException  if the operator email is taken platform-wide (409)
     */
    @Transactional
    public OperatorDto createOperator(final CreateOperatorRequest req) {
        if (operatorRepo.existsByCode(req.code())) {
            throw new OperatorCodeExistsException(req.code());
        }
        if (userRepo.findByEmail(req.operatorEmail()).isPresent()) {
            throw new EmailAlreadyRegisteredException(req.operatorEmail());
        }

        final OperatorEntity operator = operatorRepo.save(new OperatorEntity(req.code(), req.name()));

        final UserEntity user = new UserEntity();
        user.setOperatorId(operator.getId());
        user.setEmail(req.operatorEmail());
        user.setPasswordHash(passwordEncoder.encode(req.operatorPassword()));
        user.setRole(UserRole.OPERATOR);
        user.setBirthDate(LocalDate.of(1970, 1, 1)); // operator accounts are not age-gated
        user.setLocale("es");
        userRepo.save(user);

        return toDto(operator);
    }

    /** Activates/deactivates an operator (AC3); deactivation blocks its users at login. */
    @Transactional
    public OperatorDto setActive(final Long operatorId, final boolean active) {
        final OperatorEntity operator = operatorRepo.findById(operatorId)
                .orElseThrow(() -> new OperatorNotFoundException(operatorId));
        operator.setActive(active);
        return toDto(operatorRepo.save(operator));
    }

    private static OperatorDto toDto(final OperatorEntity op) {
        return new OperatorDto(op.getId(), op.getCode(), op.getName(), op.isActive(), op.getCreatedAt());
    }
}
