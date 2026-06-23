package com.novacasino.api.admin;

import com.novacasino.application.admin.AdminUseCase;
import com.novacasino.application.admin.CreateOperatorCommand;
import com.novacasino.api.admin.dto.CreateOperatorRequest;
import com.novacasino.common.dto.OperatorDto;
import com.novacasino.api.admin.dto.UpdateOperatorRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Platform-admin endpoints for multi-operator management (ADMIN role enforced by SecurityConfig). */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminUseCase admin;

    public AdminController(final AdminUseCase admin) {
        this.admin = admin;
    }

    /** GET /admin/operators — lists all operators (HU-25). */
    @GetMapping("/operators")
    public List<OperatorDto> listOperators() {
        return admin.listOperators();
    }

    /** POST /admin/operators — onboards an operator with its initial OPERATOR user (HU-25). */
    @PostMapping("/operators")
    public ResponseEntity<OperatorDto> createOperator(@Valid @RequestBody final CreateOperatorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(admin.createOperator(
                new CreateOperatorCommand(req.code(), req.name(), req.operatorEmail(), req.operatorPassword())));
    }

    /** PUT /admin/operators/{id} — activates/deactivates an operator (HU-25 AC3). */
    @PutMapping("/operators/{operatorId}")
    public OperatorDto updateOperator(@PathVariable final Long operatorId,
                                      @Valid @RequestBody final UpdateOperatorRequest req) {
        return admin.setActive(operatorId, req.active());
    }
}
