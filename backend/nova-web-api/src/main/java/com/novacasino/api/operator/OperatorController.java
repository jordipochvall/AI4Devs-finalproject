package com.novacasino.api.operator;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.idempotency.IdempotencyService;
import com.novacasino.api.operator.dto.DashboardDto;
import com.novacasino.api.operator.dto.IntegrityReportDto;
import com.novacasino.api.operator.dto.OperatorGameDto;
import com.novacasino.api.operator.dto.PlayerSummaryDto;
import com.novacasino.api.operator.dto.RechargeRequest;
import com.novacasino.api.operator.dto.ReplayDto;
import com.novacasino.api.operator.dto.RfjReportDto;
import com.novacasino.api.operator.dto.RfjReportRequest;
import com.novacasino.api.operator.dto.RoundDetailDto;
import com.novacasino.api.operator.dto.RoundSummaryDto;
import com.novacasino.api.operator.dto.UpdateGameRequest;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Operator endpoints for player management (OPERATOR role enforced by SecurityConfig). */
@RestController
@RequestMapping("/api/v1/operator")
public class OperatorController {

    private final OperatorPlayerService players;
    private final IdempotencyService idempotency;
    private final AuditService audit;
    private final ReplayService replay;
    private final OperatorGameService games;
    private final OperatorDashboardService dashboard;
    private final IntegrityService integrity;
    private final RfjReportService reports;

    public OperatorController(final OperatorPlayerService players, final IdempotencyService idempotency,
                              final AuditService audit, final ReplayService replay,
                              final OperatorGameService games, final OperatorDashboardService dashboard,
                              final IntegrityService integrity, final RfjReportService reports) {
        this.players     = players;
        this.idempotency = idempotency;
        this.audit       = audit;
        this.replay      = replay;
        this.games       = games;
        this.dashboard   = dashboard;
        this.integrity   = integrity;
        this.reports     = reports;
    }

    /** GET /operator/players — paginated search by email. */
    @GetMapping("/players")
    public PageResponse<PlayerSummaryDto> listPlayers(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @RequestParam(required = false) final String email,
            @PageableDefault(size = 20) final Pageable pageable) {
        final Long operatorId = principal.getUser().getOperatorId();
        return players.searchPlayers(operatorId, email, pageable);
    }

    /** POST /operator/players/{playerId}/wallet/recharge — idempotent recharge. */
    @PostMapping("/players/{playerId}/wallet/recharge")
    public WalletDto recharge(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long playerId,
            @RequestHeader("Idempotency-Key") final UUID idempotencyKey,
            @Valid @RequestBody final RechargeRequest body) {

        final Long operatorUserId = principal.getUser().getId();
        final Long operatorId     = principal.getUser().getOperatorId();
        final String currency     = body.currency() != null ? body.currency() : "EUR";

        final RechargeKey payload = new RechargeKey(playerId, body.amountCents(), currency);

        return idempotency.execute(
                operatorUserId, "recharge", idempotencyKey, payload, WalletDto.class,
                () -> players.recharge(operatorUserId, operatorId, playerId, body.amountCents()));
    }

    /**
     * GET /operator/rounds — paginated audit of rounds within the operator, with optional AND filters
     * (playerId, gameId, from, to). Default order is created_at DESC.
     */
    @GetMapping("/rounds")
    public PageResponse<RoundSummaryDto> listRounds(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @RequestParam(required = false) final Long playerId,
            @RequestParam(required = false) final Long gameId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to,
            @PageableDefault(size = 20) final Pageable pageable) {
        final Long operatorId = principal.getUser().getOperatorId();
        return audit.searchRounds(operatorId, playerId, gameId, from, to, pageable);
    }

    /**
     * GET /operator/rounds/{roundId}/replay — immutable record of a round for deterministic replay
     * (rendered as-is; no recomputation).
     */
    @GetMapping("/rounds/{roundId}/replay")
    public ReplayDto replayRound(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long roundId) {
        return replay.getReplay(roundId, principal.getUser().getOperatorId());
    }

    /** GET /operator/dashboard — aggregated activity KPIs, optional date window (HU-16). */
    @GetMapping("/dashboard")
    public DashboardDto getDashboard(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to) {
        return dashboard.dashboard(principal.getUser().getOperatorId(), from, to);
    }

    /** POST /operator/reports/rfj — generates the RFJ regulatory report for a month (HU-21, Fase 2). */
    @PostMapping("/reports/rfj")
    public RfjReportDto generateRfj(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @Valid @RequestBody final RfjReportRequest body) {
        return reports.generate(principal.getUser().getOperatorId(), body.year(), body.month());
    }

    /** GET /operator/audit/integrity — verifies the tamper-evident round chain over a window (HU-20). */
    @GetMapping("/audit/integrity")
    public IntegrityReportDto verifyIntegrity(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to) {
        return integrity.verify(principal.getUser().getOperatorId(), from, to);
    }

    /** GET /operator/rounds/{roundId} — single round detail without the full replay (HU-16). */
    @GetMapping("/rounds/{roundId}")
    public RoundDetailDto getRoundDetail(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long roundId) {
        return dashboard.roundDetail(roundId, principal.getUser().getOperatorId());
    }

    /** GET /operator/games — the operator's games with their commercial configuration (HU-15). */
    @GetMapping("/games")
    public List<OperatorGameDto> listGames(@AuthenticationPrincipal final NovaUserDetails principal) {
        return games.listGames(principal.getUser().getOperatorId());
    }

    /** PUT /operator/games/{gameId} — updates the commercial configuration (audited, HU-15). */
    @PutMapping("/games/{gameId}")
    public OperatorGameDto updateGame(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long gameId,
            @Valid @RequestBody final UpdateGameRequest body) {
        return games.updateGame(principal.getUser().getOperatorId(), gameId,
                principal.getUser().getId(), body);
    }

    /** Payload for the idempotency hash: reusing the same key with a different target/amount → 409. */
    private record RechargeKey(Long playerId, Long amountCents, String currency) {}
}
