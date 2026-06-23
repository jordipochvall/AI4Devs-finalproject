package com.novacasino.application.admin;

import com.novacasino.common.dto.OperatorDto;

import java.util.List;
import java.util.Optional;

/** Output port for platform-admin multi-tenant management (HU-25). */
public interface AdminPort {

    List<OperatorDto> listOperators();

    boolean codeExists(String code);

    boolean emailExists(String email);

    /** Creates the operator and its initial OPERATOR user (hashed password supplied by the use case). */
    OperatorDto createOperatorWithUser(String code, String name, String operatorEmail, String passwordHash);

    /** Activates/deactivates an operator; empty if it does not exist. */
    Optional<OperatorDto> setActive(Long operatorId, boolean active);
}
