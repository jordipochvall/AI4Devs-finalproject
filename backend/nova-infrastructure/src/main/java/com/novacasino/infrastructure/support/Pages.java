package com.novacasino.infrastructure.support;

import com.novacasino.common.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

/** Maps a Spring Data {@link Page} into the framework-agnostic {@link PageResponse} (hexagonal boundary). */
public final class Pages {

    private Pages() { }

    public static <E, T> PageResponse<T> of(final Page<E> page, final Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
