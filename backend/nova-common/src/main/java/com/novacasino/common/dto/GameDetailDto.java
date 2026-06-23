package com.novacasino.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** Game detail plus its active config (§3.3) used to render the {@code <SlotGame>}. */
public record GameDetailDto(
        Long     id,
        String   name,
        String   theme,
        String   coverImageUrl,
        long     minBetCents,
        long     maxBetCents,
        long     betStepCents,
        JsonNode config,
        /** Current progressive jackpot pool in cents, or null if the game has no jackpot (HU-26). */
        Long     jackpotCents
) {}
