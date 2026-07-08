package com.novacasino.api.it;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * HU-25 — multi-operator administration over a real DB. Uses the seeded ADMIN to onboard an operator,
 * verifies its initial user logs in over empty isolated data, that deactivation blocks login, and the
 * role/isolation rules.
 */
class AdminMultiOperatorIT extends AbstractIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@nova.test";
    private static final String ADMIN_PASS  = "admin123";

    private JsonNode createOperator(final String adminToken, final String code,
                                    final String email, final String pass) throws Exception {
        final String body = """
                {"code":"%s","name":"%s Casino","operatorEmail":"%s","operatorPassword":"%s"}"""
                .formatted(code, code, email, pass);
        final String json = mockMvc.perform(post("/api/v1/admin/operators")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty()) // regression: was null right after creation
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(json);
    }

    // --- AC1 + AC2: onboard an operator; its user logs in over empty isolated data ---

    @Test
    void onboardOperator_userLogsInOverEmptyData() throws Exception {
        final String admin = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASS);
        final String code = "acme-" + UUID.randomUUID().toString().substring(0, 8);
        final String opEmail = "op-" + UUID.randomUUID() + "@acme.test";

        createOperator(admin, code, opEmail, "Sup3rSecret!");

        // AC2: the new operator user can log in...
        final String opToken = loginAndGetToken(opEmail, "Sup3rSecret!");
        // ...and sees no players (its tenant starts empty / isolated from the seed operator).
        mockMvc.perform(get("/api/v1/operator/players")
                        .header("Authorization", "Bearer " + opToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // --- AC3: deactivating an operator blocks its users at login ---

    @Test
    void deactivatingOperator_blocksItsUsers() throws Exception {
        final String admin = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASS);
        final String code = "beta-" + UUID.randomUUID().toString().substring(0, 8);
        final String opEmail = "op-" + UUID.randomUUID() + "@beta.test";
        final long operatorId = createOperator(admin, code, opEmail, "Sup3rSecret!").get("id").asLong();

        // Works before deactivation.
        loginAndGetToken(opEmail, "Sup3rSecret!");

        mockMvc.perform(put("/api/v1/admin/operators/" + operatorId)
                        .header("Authorization", "Bearer " + admin)
                        .contentType(APPLICATION_JSON).content("""
                                {"active": false}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // After deactivation the operator user can no longer log in (403).
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(APPLICATION_JSON).content("""
                                {"email":"%s","password":"Sup3rSecret!"}""".formatted(opEmail)))
                .andExpect(status().isForbidden());
    }

    // --- AC1: duplicate operator code → 409 ---

    @Test
    void duplicateCode_returns409() throws Exception {
        final String admin = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASS);
        final String code = "dup-" + UUID.randomUUID().toString().substring(0, 8);
        createOperator(admin, code, "op-" + UUID.randomUUID() + "@dup.test", "Sup3rSecret!");

        mockMvc.perform(post("/api/v1/admin/operators")
                        .header("Authorization", "Bearer " + admin)
                        .contentType(APPLICATION_JSON).content("""
                                {"code":"%s","name":"X","operatorEmail":"%s","operatorPassword":"Sup3rSecret!"}"""
                                .formatted(code, "other-" + UUID.randomUUID() + "@dup.test")))
                .andExpect(status().isConflict());
    }

    // --- AC4: non-admin roles cannot access /admin/** ---

    @Test
    void admin_asOperator_returns403() throws Exception {
        final String operator = loginAndGetToken(OPERATOR_EMAIL, OPERATOR_PASS);
        mockMvc.perform(get("/api/v1/admin/operators")
                        .header("Authorization", "Bearer " + operator))
                .andExpect(status().isForbidden());
    }
}
