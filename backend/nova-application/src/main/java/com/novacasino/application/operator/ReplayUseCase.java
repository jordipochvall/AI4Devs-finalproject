package com.novacasino.application.operator;

import com.novacasino.application.exception.RoundNotFoundException;
import com.novacasino.common.dto.ReplayDto;
import jakarta.transaction.Transactional;

/**
 * Deterministic replay (HU-3): returns the immutable record of a round for the client to render as-is;
 * it never recomputes the spin. Over {@link ReplayPort}.
 */
public class ReplayUseCase {

    private final ReplayPort port;

    public ReplayUseCase(final ReplayPort port) {
        this.port = port;
    }

    @Transactional
    public ReplayDto getReplay(final Long roundId, final Long operatorId) {
        return port.findReplay(roundId, operatorId)
                .orElseThrow(() -> new RoundNotFoundException(roundId));
    }
}
