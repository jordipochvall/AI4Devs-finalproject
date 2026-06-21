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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for HU-25 operator onboarding in {@link AdminService}. */
class AdminServiceTest {

    private OperatorJpaRepository operatorRepo;
    private UserJpaRepository userRepo;
    private AdminService service;

    @BeforeEach
    void setUp() {
        operatorRepo = mock(OperatorJpaRepository.class);
        userRepo = mock(UserJpaRepository.class);
        service = new AdminService(operatorRepo, userRepo, new BCryptPasswordEncoder(4));
        when(operatorRepo.save(any())).thenAnswer(inv -> {
            final OperatorEntity op = inv.getArgument(0);
            setId(op, 9L);
            return op;
        });
        when(userRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createOperator_valid_createsOperatorAndInitialUser() {
        when(operatorRepo.existsByCode("acme")).thenReturn(false);
        when(userRepo.findByEmail("op@acme.test")).thenReturn(Optional.empty());

        final OperatorDto dto = service.createOperator(
                new CreateOperatorRequest("acme", "ACME Casino", "op@acme.test", "Sup3rSecret!"));

        assertThat(dto.code()).isEqualTo("acme");
        assertThat(dto.active()).isTrue();

        final ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepo).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.OPERATOR);
        assertThat(captor.getValue().getOperatorId()).isEqualTo(9L);
        // The password is hashed, not stored in clear.
        assertThat(captor.getValue().getPasswordHash()).isNotEqualTo("Sup3rSecret!");
    }

    @Test
    void createOperator_duplicateCode_throwsConflict() {
        when(operatorRepo.existsByCode("acme")).thenReturn(true);
        assertThatThrownBy(() -> service.createOperator(
                new CreateOperatorRequest("acme", "ACME", "op@acme.test", "Sup3rSecret!")))
                .isInstanceOf(OperatorCodeExistsException.class);
        verify(operatorRepo, never()).save(any());
    }

    @Test
    void createOperator_duplicateEmail_throwsConflict() {
        when(operatorRepo.existsByCode("acme")).thenReturn(false);
        when(userRepo.findByEmail("op@acme.test")).thenReturn(Optional.of(new UserEntity()));
        assertThatThrownBy(() -> service.createOperator(
                new CreateOperatorRequest("acme", "ACME", "op@acme.test", "Sup3rSecret!")))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void setActive_unknownOperator_throwsNotFound() {
        when(operatorRepo.findById(5L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.setActive(5L, false))
                .isInstanceOf(OperatorNotFoundException.class);
    }

    private static void setId(final Object entity, final Long id) {
        try {
            final var f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
