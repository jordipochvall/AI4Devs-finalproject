package com.novacasino.api.player;

import com.novacasino.api.player.dto.GameDetailDto;
import com.novacasino.api.player.dto.GameSummaryDto;
import com.novacasino.api.player.dto.SpinRequest;
import com.novacasino.api.player.dto.SpinResultDto;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.security.NovaUserDetails;
import jakarta.validation.Valid;
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

    private final PlayerCatalogService catalog;
    private final SpinService spinService;

    public PlayerController(final PlayerCatalogService catalog, final SpinService spinService) {
        this.catalog = catalog;
        this.spinService = spinService;
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
}
