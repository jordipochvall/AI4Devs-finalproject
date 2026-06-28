package com.novacasino.application.admin;

import com.novacasino.application.admin.exception.OperatorCodeExistsException;
import com.novacasino.application.admin.exception.OperatorNotFoundException;
import com.novacasino.application.auth.PasswordHasherPort;
import com.novacasino.application.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.common.dto.OperatorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Unit tests for HU-25 multi-tenant onboarding in {@link AdminUseCase} (port + hasher mocked). */
class AdminUseCaseTest {

    private AdminPort port;
    private PasswordHasherPort hasher;
    private AdminUseCase useCase;

    @BeforeEach
    void setUp() {
        port = mock(AdminPort.class);
        hasher = mock(PasswordHasherPort.class);
        useCase = new AdminUseCase(port, hasher);
    }

    private static CreateOperatorCommand cmd() {
        return new CreateOperatorCommand("acme", "Acme", "ops@acme.com", "secret");
    }

    @Test
    void createOperator_codeExists_throws() {
        when(port.codeExists("acme")).thenReturn(true);
        assertThatThrownBy(() -> useCase.createOperator(cmd()))
                .isInstanceOf(OperatorCodeExistsException.class);
        verify(port, never()).createOperatorWithUser(any(), any(), any(), any());
    }

    @Test
    void createOperator_emailExists_throws() {
        when(port.codeExists("acme")).thenReturn(false);
        when(port.emailExists("ops@acme.com")).thenReturn(true);
        assertThatThrownBy(() -> useCase.createOperator(cmd()))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
        verify(port, never()).createOperatorWithUser(any(), any(), any(), any());
    }

    @Test
    void createOperator_valid_hashesAndCreates() {
        when(port.codeExists("acme")).thenReturn(false);
        when(port.emailExists("ops@acme.com")).thenReturn(false);
        when(hasher.hash("secret")).thenReturn("HASHED");
        final OperatorDto dto = new OperatorDto(1L, "acme", "Acme", true, OffsetDateTime.now());
        when(port.createOperatorWithUser("acme", "Acme", "ops@acme.com", "HASHED")).thenReturn(dto);

        assertThat(useCase.createOperator(cmd()).id()).isEqualTo(1L);
        verify(port).createOperatorWithUser("acme", "Acme", "ops@acme.com", "HASHED");
    }

    @Test
    void setActive_notFound_throws() {
        when(port.setActive(eq(9L), anyBoolean())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.setActive(9L, false))
                .isInstanceOf(OperatorNotFoundException.class);
    }

    @Test
    void setActive_present_returnsUpdatedOperator() {
        final OperatorDto deactivated = new OperatorDto(1L, "acme", "Acme", false, OffsetDateTime.now());
        when(port.setActive(1L, false)).thenReturn(Optional.of(deactivated));

        assertThat(useCase.setActive(1L, false).active()).isFalse();
        verify(port).setActive(1L, false);
    }
}
