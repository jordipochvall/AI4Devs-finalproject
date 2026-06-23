package com.novacasino.common.dto;

import java.util.List;

/** Standard pagination wrapper for the API (readme §4.1). Pure DTO (no framework deps). */
public record PageResponse<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements,
        int     totalPages
) {
}
