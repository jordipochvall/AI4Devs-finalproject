package com.novacasino.application.operator;

import com.novacasino.common.dto.OperatorGameDto;

import java.util.List;
import java.util.Optional;

/** Output port for operator commercial-config read/update (HU-15). */
public interface OperatorGamePort {

    List<OperatorGameDto> listGames(Long operatorId);

    /** Payline count of the game's active config (null when none); empty if the game is not in the operator. */
    Optional<Integer> findGamePaylineCount(Long operatorId, Long gameId);

    /** Applies the update, records the before/after audit and returns the resulting DTO. */
    OperatorGameDto applyUpdate(Long operatorId, Long gameId, Long performedByUserId, GameCommercialUpdate update);
}
