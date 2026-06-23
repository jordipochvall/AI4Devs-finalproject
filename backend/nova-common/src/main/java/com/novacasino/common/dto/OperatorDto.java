package com.novacasino.common.dto;

import java.time.OffsetDateTime;

/** An operator (tenant) as seen by the platform admin (HU-25). */
public record OperatorDto(Long id, String code, String name, boolean active, OffsetDateTime createdAt) {
}
