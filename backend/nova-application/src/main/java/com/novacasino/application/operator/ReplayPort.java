package com.novacasino.application.operator;

import com.novacasino.common.dto.ReplayDto;

import java.util.Optional;

/** Output port for the deterministic round replay (HU-3). */
public interface ReplayPort {

    /** The immutable replay record of a round owned by the operator; empty if missing or foreign. */
    Optional<ReplayDto> findReplay(Long roundId, Long operatorId);
}
