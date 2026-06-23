package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/** Result of publishing a math version (HU-17): the now-active version and the publication record. */
public record PublishResultDto(
        Long gameId,
        Long activeConfigId,
        int version,
        Long publishedByUserId,
        OffsetDateTime publishedAt
) {
}
