package com.novacasino.api.player;

import com.novacasino.common.dto.PageRequestDto;
import com.novacasino.common.dto.PageResponse;
import com.novacasino.common.dto.PlayerRoundDto;
import com.novacasino.common.dto.WalletTransactionDto;
import com.novacasino.application.player.PlayerCatalogUseCase;
import com.novacasino.application.player.PlayerHistoryUseCase;
import com.novacasino.application.player.ResponsibleGamingUseCase;
import com.novacasino.common.dto.GameDetailDto;
import com.novacasino.common.dto.GameSummaryDto;
import com.novacasino.common.dto.LimitDto;
import com.novacasino.common.dto.SelfExclusionDto;
import com.novacasino.api.player.dto.SelfExclusionRequest;
import com.novacasino.api.player.dto.SetLimitRequest;
import com.novacasino.api.player.dto.SpinRequest;
import com.novacasino.common.dto.SpinResultDto;
import com.novacasino.common.dto.WalletDto;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Player endpoints (PLAYER role enforced by SecurityConfig on /player/**). */
@RestController
@RequestMapping("/api/v1/player")
public class PlayerController {

    private final PlayerCatalogUseCase catalog;
    private final SpinService spinService;
    private final PlayerHistoryUseCase history;
    private final ResponsibleGamingUseCase responsibleGaming;

    public PlayerController(final PlayerCatalogUseCase catalog, final SpinService spinService,
                           final PlayerHistoryUseCase history,
                           final ResponsibleGamingUseCase responsibleGaming) {
        this.catalog = catalog;
        this.spinService = spinService;
        this.history = history;
        this.responsibleGaming = responsibleGaming;
    }

    /** GET /player/games — active games for the lobby. */
    @GetMapping("/games")
    public List<GameSummaryDto> listGames() {
        return catalog.listActiveGames();
    }

    /** GET /player/games/{id} — game detail plus its active config. */
    @GetMapping("/games/{gameId}")
    public GameDetailDto getGame(@PathVariable final Long gameId) {
        return catalog.getActiveGame(gameId);
    }

    /** GET /player/wallet — balance of the authenticated player. */
    @GetMapping("/wallet")
    public WalletDto getWallet(@AuthenticationPrincipal final NovaUserDetails principal) {
        return catalog.getWallet(principal.getUser().getId());
    }

    /** POST /player/games/{gameId}/spin — executes a spin (idempotent, resolves free spins). */
    @PostMapping("/games/{gameId}/spin")
    public SpinResultDto spin(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PathVariable final Long gameId,
            @RequestHeader("Idempotency-Key") final UUID idempotencyKey,
            @Valid @RequestBody final SpinRequest body) {

        final Long userId = principal.getUser().getId();
        final Long operatorId = principal.getUser().getOperatorId();
        final String currency = body.currency() != null ? body.currency() : "EUR";
        return spinService.spin(userId, operatorId, gameId, idempotencyKey, body.betCents(), currency);
    }

    /** GET /player/wallet/transactions — the player's own ledger movements, paginated (HU-14). */
    @GetMapping("/wallet/transactions")
    public PageResponse<WalletTransactionDto> listTransactions(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PageableDefault(size = 20) final Pageable pageable) {
        return history.listTransactions(principal.getUser().getId(),
                new PageRequestDto(pageable.getPageNumber(), pageable.getPageSize()));
    }

    /** GET /player/rounds — the player's own rounds, paginated (HU-14). */
    @GetMapping("/rounds")
    public PageResponse<PlayerRoundDto> listRounds(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @PageableDefault(size = 20) final Pageable pageable) {
        return history.listRounds(principal.getUser().getId(),
                new PageRequestDto(pageable.getPageNumber(), pageable.getPageSize()));
    }

    /** POST /player/limits — sets/changes a responsible-gaming limit (HU-19, Fase 2). */
    @PostMapping("/limits")
    public LimitDto setLimit(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @Valid @RequestBody final SetLimitRequest body) {
        return responsibleGaming.setLimit(principal.getUser().getId(),
                body.limitType(), body.period(), body.amountCents());
    }

    /** POST /player/self-exclusion — registers a self-exclusion period (HU-19, Fase 2). */
    @PostMapping("/self-exclusion")
    public SelfExclusionDto selfExclude(
            @AuthenticationPrincipal final NovaUserDetails principal,
            @Valid @RequestBody final SelfExclusionRequest body) {
        return responsibleGaming.setSelfExclusion(principal.getUser().getId(), body.days());
    }
}
