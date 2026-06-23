package com.novacasino.application.player;

import com.novacasino.common.dto.GameDetailDto;
import com.novacasino.common.dto.GameSummaryDto;
import com.novacasino.common.dto.WalletDto;

import java.util.List;
import java.util.Optional;

/** Output port for the player's read catalogue + wallet (HU-5). Implemented in infrastructure. */
public interface PlayerCatalogPort {

    /** Active games for the lobby (exposing only the grid). */
    List<GameSummaryDto> listActiveGames();

    /** Active game detail + its config (+ jackpot); empty if missing, inactive or without active config. */
    Optional<GameDetailDto> activeGame(Long gameId);

    /** The player's wallet balance. */
    Optional<WalletDto> walletOf(Long userId);
}
