package com.novacasino.api.operator;

import com.novacasino.api.common.PageResponse;
import com.novacasino.api.idempotency.IdempotencyService;
import com.novacasino.api.operator.dto.PlayerSummaryDto;
import com.novacasino.api.operator.dto.RechargeRequest;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/** Operator endpoints for player management (OPERATOR role enforced by SecurityConfig). */
@RestController
@RequestMapping("/api/v1/operator")
public class OperatorController {

    private final OperatorPlayerService players;
    private final IdempotencyService idempotency;

    public OperatorController(final OperatorPlayerService players, final IdempotencyService idempotency) {
        this.players     = players;
        this.idempotency = idempotency;
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

    /** Payload for the idempotency hash: reusing the same key with a different target/amount → 409. */
    private record RechargeKey(Long playerId, Long amountCents, String currency) {}
}
