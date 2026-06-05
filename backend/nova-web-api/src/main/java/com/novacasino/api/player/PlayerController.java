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

/**
 * Endpoints de lectura del jugador (rol PLAYER, exigido por SecurityConfig en /player/**).
 */
@RestController
@RequestMapping("/api/v1/player")
public class PlayerController {

    private final PlayerCatalogService catalog;

    public PlayerController(PlayerCatalogService catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/games")
    public List<GameSummaryDto> listGames() {
        return catalog.listActiveGames();
    }

    @GetMapping("/games/{gameId}")
    public GameDetailDto getGame(@PathVariable Long gameId) {
        return catalog.getActiveGame(gameId);
    }

    @GetMapping("/wallet")
    public WalletDto getWallet(@AuthenticationPrincipal NovaUserDetails principal) {
        return catalog.getWallet(principal.getUser().getId());
    }
}
