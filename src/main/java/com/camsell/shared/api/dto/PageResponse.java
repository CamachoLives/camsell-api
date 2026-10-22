package com.camsell.shared.api.dto;

import com.camsell.shared.domain.page.PageResult;

import java.util.List;

/** Envoltura estable de paginacion expuesta por la API publica. */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {

    public static <T> PageResponse<T> from(PageResult<T> result) {
        return new PageResponse<>(
                result.content(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.page() == 0,
                result.totalPages() == 0 || result.page() >= result.totalPages() - 1);
    }
}
