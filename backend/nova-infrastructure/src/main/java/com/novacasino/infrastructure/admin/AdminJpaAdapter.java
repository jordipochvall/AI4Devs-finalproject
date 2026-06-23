package com.novacasino.infrastructure.admin;

import com.novacasino.application.admin.AdminPort;
import com.novacasino.common.dto.OperatorDto;
import com.novacasino.domain.user.UserRole;
import com.novacasino.infrastructure.persistence.entity.OperatorEntity;
import com.novacasino.infrastructure.persistence.entity.UserEntity;
import com.novacasino.infrastructure.persistence.repository.OperatorJpaRepository;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** JPA adapter for {@link AdminPort} (HU-25): operator listing/onboarding and activation. */
@Component
public class AdminJpaAdapter implements AdminPort {

    private final OperatorJpaRepository operatorRepo;
    private final UserJpaRepository userRepo;

    public AdminJpaAdapter(final OperatorJpaRepository operatorRepo, final UserJpaRepository userRepo) {
        this.operatorRepo = operatorRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<OperatorDto> listOperators() {
        return operatorRepo.findAllByOrderByIdAsc().stream().map(AdminJpaAdapter::toDto).toList();
    }

    @Override
    public boolean codeExists(final String code) {
        return operatorRepo.existsByCode(code);
    }

    @Override
    public boolean emailExists(final String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    @Override
    public OperatorDto createOperatorWithUser(final String code, final String name,
                                              final String operatorEmail, final String passwordHash) {
        final OperatorEntity operator = operatorRepo.save(new OperatorEntity(code, name));

        final UserEntity user = new UserEntity();
        user.setOperatorId(operator.getId());
        user.setEmail(operatorEmail);
        user.setPasswordHash(passwordHash);
        user.setRole(UserRole.OPERATOR);
        user.setBirthDate(LocalDate.of(1970, 1, 1)); // operator accounts are not age-gated
        user.setLocale("es");
        userRepo.save(user);

        return toDto(operator);
    }

    @Override
    public Optional<OperatorDto> setActive(final Long operatorId, final boolean active) {
        return operatorRepo.findById(operatorId).map(operator -> {
            operator.setActive(active);
            return toDto(operatorRepo.save(operator));
        });
    }

    private static OperatorDto toDto(final OperatorEntity op) {
        return new OperatorDto(op.getId(), op.getCode(), op.getName(), op.isActive(), op.getCreatedAt());
    }
}
