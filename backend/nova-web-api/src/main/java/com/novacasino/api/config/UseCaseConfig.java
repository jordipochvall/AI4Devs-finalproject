package com.novacasino.api.config;

import com.novacasino.application.admin.AdminPort;
import com.novacasino.application.admin.AdminUseCase;
import com.novacasino.application.audit.AuditChainPort;
import com.novacasino.application.audit.VerifyIntegrityUseCase;
import com.novacasino.application.auth.PasswordHasherPort;
import com.novacasino.application.math.ExplainUseCase;
import com.novacasino.application.math.MathConfigPort;
import com.novacasino.application.math.MathConfigUseCase;
import com.novacasino.application.math.SimulationHistoryUseCase;
import com.novacasino.application.math.SimulationLaunchPort;
import com.novacasino.application.math.SimulationQueryPort;
import com.novacasino.application.math.SimulationUseCase;
import com.novacasino.application.math.validation.ConfigValidator;
import com.novacasino.domain.ai.Explainer;

import java.util.Optional;
import com.novacasino.application.operator.AuditRoundsPort;
import com.novacasino.application.operator.AuditUseCase;
import com.novacasino.application.operator.OperatorGamePort;
import com.novacasino.application.operator.OperatorGameUseCase;
import com.novacasino.application.operator.OperatorDashboardPort;
import com.novacasino.application.operator.OperatorDashboardUseCase;
import com.novacasino.application.operator.OperatorPlayerPort;
import com.novacasino.application.operator.OperatorPlayerUseCase;
import com.novacasino.application.operator.ReplayPort;
import com.novacasino.application.operator.ReplayUseCase;
import com.novacasino.application.operator.RfjAggregatesPort;
import com.novacasino.application.operator.RfjReportUseCase;
import com.novacasino.application.auth.RefreshTokenStorePort;
import com.novacasino.application.auth.RefreshTokenUseCase;
import com.novacasino.application.player.PlayerCatalogPort;
import com.novacasino.application.player.PlayerCatalogUseCase;
import com.novacasino.application.player.PlayerHistoryPort;
import com.novacasino.application.player.PlayerHistoryUseCase;
import com.novacasino.application.player.ResponsibleGamingPort;
import com.novacasino.application.player.ResponsibleGamingUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the {@code nova-application} use cases as Spring beans, injecting the infrastructure adapters
 * that implement their ports. Use cases are framework-agnostic POJOs (no Spring stereotypes), so the
 * web layer assembles them here — keeping the hexagonal dependency direction (application never
 * depends on infrastructure or web).
 */
@Configuration
public class UseCaseConfig {

    @Bean
    VerifyIntegrityUseCase verifyIntegrityUseCase(final AuditChainPort auditChainPort) {
        return new VerifyIntegrityUseCase(auditChainPort);
    }

    @Bean
    AdminUseCase adminUseCase(final AdminPort adminPort, final PasswordHasherPort passwordHasherPort) {
        return new AdminUseCase(adminPort, passwordHasherPort);
    }

    @Bean
    RefreshTokenUseCase refreshTokenUseCase(
            final RefreshTokenStorePort refreshTokenStorePort,
            @Value("${app.jwt.refresh-ttl-seconds:2592000}") final long refreshTtlSeconds) {
        return new RefreshTokenUseCase(refreshTokenStorePort, refreshTtlSeconds);
    }

    @Bean
    PlayerCatalogUseCase playerCatalogUseCase(final PlayerCatalogPort playerCatalogPort) {
        return new PlayerCatalogUseCase(playerCatalogPort);
    }

    @Bean
    AuditUseCase auditUseCase(final AuditRoundsPort auditRoundsPort) {
        return new AuditUseCase(auditRoundsPort);
    }

    @Bean
    OperatorGameUseCase operatorGameUseCase(final OperatorGamePort operatorGamePort) {
        return new OperatorGameUseCase(operatorGamePort);
    }

    @Bean
    OperatorPlayerUseCase operatorPlayerUseCase(final OperatorPlayerPort operatorPlayerPort) {
        return new OperatorPlayerUseCase(operatorPlayerPort);
    }

    @Bean
    OperatorDashboardUseCase operatorDashboardUseCase(final OperatorDashboardPort operatorDashboardPort) {
        return new OperatorDashboardUseCase(operatorDashboardPort);
    }

    @Bean
    ReplayUseCase replayUseCase(final ReplayPort replayPort) {
        return new ReplayUseCase(replayPort);
    }

    @Bean
    RfjReportUseCase rfjReportUseCase(final VerifyIntegrityUseCase verifyIntegrityUseCase,
                                      final RfjAggregatesPort rfjAggregatesPort) {
        return new RfjReportUseCase(verifyIntegrityUseCase, rfjAggregatesPort);
    }

    @Bean
    ConfigValidator configValidator() {
        return new ConfigValidator();
    }

    @Bean
    MathConfigUseCase mathConfigUseCase(final MathConfigPort mathConfigPort, final ConfigValidator configValidator) {
        return new MathConfigUseCase(mathConfigPort, configValidator);
    }

    @Bean
    SimulationHistoryUseCase simulationHistoryUseCase(final SimulationQueryPort simulationQueryPort) {
        return new SimulationHistoryUseCase(simulationQueryPort);
    }

    @Bean
    SimulationUseCase simulationUseCase(final SimulationLaunchPort simulationLaunchPort) {
        return new SimulationUseCase(simulationLaunchPort);
    }

    @Bean
    ExplainUseCase explainUseCase(final SimulationQueryPort simulationQueryPort,
                                  final Optional<Explainer> explainer) {
        return new ExplainUseCase(simulationQueryPort, explainer);
    }

    @Bean
    PlayerHistoryUseCase playerHistoryUseCase(final PlayerHistoryPort playerHistoryPort) {
        return new PlayerHistoryUseCase(playerHistoryPort);
    }

    @Bean
    ResponsibleGamingUseCase responsibleGamingUseCase(
            final ResponsibleGamingPort responsibleGamingPort,
            @Value("${app.responsible-gaming.cooldown-seconds:86400}") final long cooldownSeconds) {
        return new ResponsibleGamingUseCase(responsibleGamingPort, cooldownSeconds);
    }
}
