package com.novacasino.application.admin;

import com.novacasino.application.admin.exception.OperatorCodeExistsException;
import com.novacasino.application.admin.exception.OperatorNotFoundException;
import com.novacasino.application.auth.PasswordHasherPort;
import com.novacasino.application.auth.exception.EmailAlreadyRegisteredException;
import com.novacasino.common.dto.OperatorDto;
import jakarta.transaction.Transactional;

import java.util.List;

/**
 * Platform-admin use cases for multi-tenant management (HU-25): list operators and onboard a new
 * operator together with its initial OPERATOR user. New operators start active and isolated; their
 * users authenticate by email across tenants (see the login flow).
 */
public class AdminUseCase {

    private final AdminPort port;
    private final PasswordHasherPort hasher;

    public AdminUseCase(final AdminPort port, final PasswordHasherPort hasher) {
        this.port = port;
        this.hasher = hasher;
    }

    @Transactional
    public List<OperatorDto> listOperators() {
        return port.listOperators();
    }

    /**
     * Creates an operator and its initial OPERATOR user (AC1).
     *
     * @throws OperatorCodeExistsException     if the code is taken (409)
     * @throws EmailAlreadyRegisteredException if the operator email is taken platform-wide (409)
     */
    @Transactional
    public OperatorDto createOperator(final CreateOperatorCommand cmd) {
        if (port.codeExists(cmd.code())) {
            throw new OperatorCodeExistsException(cmd.code());
        }
        if (port.emailExists(cmd.operatorEmail())) {
            throw new EmailAlreadyRegisteredException(cmd.operatorEmail());
        }
        return port.createOperatorWithUser(cmd.code(), cmd.name(), cmd.operatorEmail(),
                hasher.hash(cmd.operatorPassword()));
    }

    /** Activates/deactivates an operator (AC3); deactivation blocks its users at login. */
    @Transactional
    public OperatorDto setActive(final Long operatorId, final boolean active) {
        return port.setActive(operatorId, active)
                .orElseThrow(() -> new OperatorNotFoundException(operatorId));
    }
}
