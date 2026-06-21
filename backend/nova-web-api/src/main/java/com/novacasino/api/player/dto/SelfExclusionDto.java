package com.novacasino.api.player.dto;

import java.time.OffsetDateTime;

/** A player's active self-exclusion period (HU-19). */
public record SelfExclusionDto(OffsetDateTime startAt, OffsetDateTime endAt) {
}
