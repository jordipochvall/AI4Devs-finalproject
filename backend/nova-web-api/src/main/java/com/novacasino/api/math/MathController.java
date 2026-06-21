package com.novacasino.api.math;

import com.novacasino.api.math.dto.ConfigCreatedDto;
import com.novacasino.api.math.dto.ConfigDetailDto;
import com.novacasino.api.math.dto.ConfigVersionDto;
import com.novacasino.api.math.dto.CreateConfigRequest;
import com.novacasino.api.math.dto.ExplainRequest;
import com.novacasino.api.math.dto.ExplanationDto;
import com.novacasino.api.math.dto.LaunchSimulationRequest;
import com.novacasino.api.math.dto.MathGameDto;
import com.novacasino.api.math.dto.PublishRequest;
import com.novacasino.api.math.dto.PublishResultDto;
import com.novacasino.api.math.dto.SimulationAcceptedDto;
import com.novacasino.api.math.dto.SimulationStatusDto;
import com.novacasino.api.math.dto.SimulationSummaryDto;
import com.novacasino.api.common.PageResponse;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Math backoffice (MATH_ANALYST role enforced by SecurityConfig on /math/**).
 * Publishing versions is post-MVP.
 */
@RestController
@RequestMapping("/api/v1/math")
public class MathController {

    private final MathService math;
    private final SimulationService simulations;
    private final SimulationHistoryService simulationHistory;
    private final ExplainService explain;

    public MathController(final MathService math, final SimulationService simulations,
                          final SimulationHistoryService simulationHistory, final ExplainService explain) {
        this.math = math;
        this.simulations = simulations;
        this.simulationHistory = simulationHistory;
        this.explain = explain;
    }

    /** GET /math/games — games with their active version. */
    @GetMapping("/games")
    public List<MathGameDto> listGames(@AuthenticationPrincipal final NovaUserDetails principal) {
        return math.listGames(principal.getUser().getOperatorId());
    }

    /** GET /math/configs/{id} — detail of a config version. */
    @GetMapping("/configs/{configId}")
    public ConfigDetailDto getConfig(@PathVariable final Long configId) {
        return math.getConfig(configId);
    }

    /** GET /math/games/{id}/configs — math version history of a game, with the active one flagged (HU-17). */
    @GetMapping("/games/{gameId}/configs")
    public List<ConfigVersionDto> listConfigs(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long gameId) {
        return math.listConfigs(gameId, principal.getUser().getOperatorId());
    }

    /** POST /math/games/{id}/publish — activates a math version for the game (HU-17). */
    @PostMapping("/games/{gameId}/publish")
    public PublishResultDto publish(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long gameId,
            @Valid @RequestBody final PublishRequest body) {
        return math.publish(gameId, body.configId(),
                principal.getUser().getOperatorId(), principal.getUser().getId());
    }

    /** POST /math/games/{id}/configs — creates a new math version. */
    @PostMapping("/games/{gameId}/configs")
    public ResponseEntity<ConfigCreatedDto> createConfig(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long gameId,
            @Valid @RequestBody final CreateConfigRequest body) {
        final Long operatorId = principal.getUser().getOperatorId();
        final Long mathUserId = principal.getUser().getId();
        final ConfigCreatedDto created = math.createConfig(gameId, operatorId, mathUserId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** POST /math/configs/{id}/simulations — launches a mass simulation (async, 202). */
    @PostMapping("/configs/{configId}/simulations")
    public ResponseEntity<SimulationAcceptedDto> launchSimulation(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long configId,
            @Valid @RequestBody final LaunchSimulationRequest body) {
        final SimulationAcceptedDto accepted = simulations.launch(
                principal.getUser().getOperatorId(), principal.getUser().getId(),
                configId, body.numSpins(), body.betCents());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(accepted);
    }

    /** GET /math/simulations — operator's simulation history, paginated, filterable (HU-18). */
    @GetMapping("/simulations")
    public PageResponse<SimulationSummaryDto> listSimulations(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @RequestParam(required = false) final Long configId,
            @RequestParam(required = false) final Long gameId,
            @PageableDefault(size = 20) final Pageable pageable) {
        return simulationHistory.listSimulations(
                principal.getUser().getOperatorId(), configId, gameId, pageable);
    }

    /** GET /math/simulations/{id}/explanations — the AI Q&A thread of a simulation (HU-18). */
    @GetMapping("/simulations/{simulationId}/explanations")
    public List<ExplanationDto> listExplanations(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long simulationId) {
        return simulationHistory.listExplanations(simulationId, principal.getUser().getOperatorId());
    }

    /** GET /math/simulations/{id} — status and, once completed, the metrics (polling). */
    @GetMapping("/simulations/{simulationId}")
    public SimulationStatusDto getSimulation(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long simulationId) {
        return simulations.getSimulation(simulationId, principal.getUser().getOperatorId());
    }

    /** POST /math/simulations/{id}/explain — AI explanation of a completed simulation (HU-8). */
    @PostMapping("/simulations/{simulationId}/explain")
    public ExplanationDto explainSimulation(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long simulationId,
            @Valid @RequestBody final ExplainRequest body) {
        return explain.explain(simulationId, principal.getUser().getOperatorId(),
                principal.getUser().getId(), body.question());
    }
}
