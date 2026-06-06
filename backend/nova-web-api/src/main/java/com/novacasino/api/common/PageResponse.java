package com.novacasino.api.common;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Standard pagination wrapper for the API (see readme §4.1). */
public record PageResponse<T>(
        List<T> content,
        int     page,
        int     size,
        long    totalElements,
        int     totalPages
) {
    /** Builds a PageResponse by mapping the elements of a Spring Data {@link Page}. */
    public static <E, T> PageResponse<T> of(final Page<E> page, final Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
