package com.novacasino.application.operator;

import com.novacasino.application.exception.GameNotFoundException;
import com.novacasino.application.operator.exception.InvalidCommercialConfigException;
import com.novacasino.common.dto.OperatorGameDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Operator commercial-config use cases (HU-15): list and update bet bounds, currencies and the active
 * flag (never the math). Coherence rules (max >= min, bet/step multiples of the payline count) are
 * enforced here; persistence and the append-only before/after audit live in the adapter.
 */
public class OperatorGameUseCase {

    private static final Logger log = LoggerFactory.getLogger(OperatorGameUseCase.class);

    private final OperatorGamePort port;

    public OperatorGameUseCase(final OperatorGamePort port) {
        this.port = port;
    }

    @Transactional
    public List<OperatorGameDto> listGames(final Long operatorId) {
        return port.listGames(operatorId);
    }

    @Transactional
    public OperatorGameDto updateGame(final Long operatorId, final Long gameId,
                                      final Long performedByUserId, final GameCommercialUpdate update) {
        // AC5: a game outside the operator is "not found"; findGamePaylineCount returns empty for it.
        final Optional<Integer> paylineCount = port.findGamePaylineCount(operatorId, gameId);
        if (paylineCount.isEmpty()) {
            throw new GameNotFoundException(gameId);
        }
        validate(update, paylineCount.get());
        final OperatorGameDto updated = port.applyUpdate(operatorId, gameId, performedByUserId, update);
        log.info("Commercial config updated: gameId={}, operatorId={}, active={}, by userId={}",
                gameId, operatorId, updated.active(), performedByUserId);
        return updated;
    }

    /** Coherence checks (AC3): ordered positive bounds and bet/step multiples of the payline count. */
    private void validate(final GameCommercialUpdate req, final Integer paylineCount) {
        if (req.maxBetCents() < req.minBetCents()) {
            throw new InvalidCommercialConfigException("maxBetCents must be >= minBetCents");
        }
        if (paylineCount != null && paylineCount > 0) {
            if (req.minBetCents() % paylineCount != 0) {
                throw new InvalidCommercialConfigException("minBetCents must be a multiple of the payline count");
            }
            if (req.betStepCents() % paylineCount != 0) {
                throw new InvalidCommercialConfigException("betStepCents must be a multiple of the payline count");
            }
        }
    }
}
