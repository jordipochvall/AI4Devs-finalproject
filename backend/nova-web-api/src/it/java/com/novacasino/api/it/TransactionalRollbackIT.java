package com.novacasino.api.it;

import com.novacasino.application.admin.AdminUseCase;
import com.novacasino.application.admin.CreateOperatorCommand;
import com.novacasino.infrastructure.persistence.repository.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Atomicity of a multi-write use case (T1): onboarding an operator writes the {@code operators} row and
 * the initial {@code users} row, which must commit together. This forces the second write (the user
 * insert) to fail and asserts the first write (the operator) is rolled back — i.e. no orphan operator.
 *
 * <p>{@link UserJpaRepository} is mocked so the user insert throws; the operator repository is real.
 */
class TransactionalRollbackIT extends AbstractIntegrationTest {

    @MockitoBean
    private UserJpaRepository userRepo;

    @Autowired
    private AdminUseCase adminUseCase;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void createOperator_whenUserInsertFails_rollsBackTheOperator() {
        // emailExists() must say "no" so we reach the two writes; the user insert then blows up.
        when(userRepo.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepo.save(any())).thenThrow(new RuntimeException("simulated user insert failure"));

        final String code = "rollback-it-op";
        assertThatThrownBy(() -> adminUseCase.createOperator(
                new CreateOperatorCommand(code, "Rollback IT", "rollback-it@nova.test", "secret123")))
                .isInstanceOf(RuntimeException.class);

        // The operator insert must have been rolled back together with the failed user insert.
        final Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM operators WHERE code = ?", Integer.class, code);
        assertThat(count).isZero();
    }
}
