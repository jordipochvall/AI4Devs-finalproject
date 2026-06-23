package com.novacasino.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Immutable record of a round for the deterministic replay (readme §4.4.5). The {@code result} is
 * authoritative — exactly what the player saw — and is rendered as-is by {@code <SlotGame mode="replay">}.
 * {@code rngSeed} and {@code config} are forensic/verification metadata; the endpoint does not
 * recompute the spin.
 *
 * @param roundId      the round id
 * @param gameId       the game played
 * @param gameConfigId the exact math version used (not necessarily the active one)
 * @param rngSeed      the seed of the round (forensic metadata, not used to render)
 * @param result       the stored, authoritative outcome (with free-spin children reconstructed)
 * @param config       the exact config JSON of {@code gameConfigId} (context/verification)
 */
public record ReplayDto(
        Long roundId,
        Long gameId,
        Long gameConfigId,
        long rngSeed,
        SpinResultDto result,
        JsonNode config) {
}
