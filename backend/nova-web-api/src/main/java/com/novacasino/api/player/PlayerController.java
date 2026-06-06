package com.novacasino.api.player;

import com.novacasino.api.player.dto.GameDetailDto;
import com.novacasino.api.player.dto.GameSummaryDto;
import com.novacasino.api.player.dto.WalletDto;
import com.novacasino.api.security.NovaUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Player read endpoints (PLAYER role enforced by SecurityConfig on /player/**). */
@RestController
@RequestMapping("/api/v1/player")
public class PlayerController {

    private final PlayerCatalogService catalog;

    public PlayerController(final PlayerCatalogService catalog) {
        this.catalog = catalog;
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
}
