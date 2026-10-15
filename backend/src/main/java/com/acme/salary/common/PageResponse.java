package com.acme.salary.common;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * One page of results. A dedicated type keeps the JSON contract stable,
 * instead of exposing Spring's internal Page structure to the frontend.
 */
public record PageResponse<T>(List<T> items, int page, int size, long totalItems, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
