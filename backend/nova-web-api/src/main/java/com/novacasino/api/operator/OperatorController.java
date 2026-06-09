package com.novacasino.api.operator;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.idempotency.IdempotencyService;
import com.novacasino.api.operator.dto.PlayerSummaryDto;
import com.novacasino.api.operator.dto.RechargeRequest;
import com.novacasino.api.operator.dto.ReplayDto;
import com.novacasino.api.operator.dto.RoundSummaryDto;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Operator endpoints for player management (OPERATOR role enforced by SecurityConfig). */
@RestController
@RequestMapping("/api/v1/operator")
public class OperatorController {

    private final OperatorPlayerService players;
    private final IdempotencyService idempotency;
    private final AuditService audit;
    private final ReplayService replay;

    public OperatorController(final OperatorPlayerService players, final IdempotencyService idempotency,
                              final AuditService audit, final ReplayService replay) {
        this.players     = players;
        this.idempotency = idempotency;
        this.audit       = audit;
        this.replay      = replay;
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

    /** Payload for the idempotency hash: reusing the same key with a different target/amount → 409. */
    private record RechargeKey(Long playerId, Long amountCents, String currency) {}
}
