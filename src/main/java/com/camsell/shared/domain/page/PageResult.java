package com.camsell.shared.domain.page;

import java.util.List;
import java.util.function.Function;

/** Resultado paginado agnostico de framework. */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(List.of(), query.page(), query.size(), 0, 0);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
