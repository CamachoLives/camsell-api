package com.camsell.shared.infrastructure.persistence;

import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import com.camsell.shared.domain.page.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.function.Function;

/**
 * Unico punto de traduccion entre la paginacion del dominio y Spring Data. De
 * esta forma ningun puerto filtra tipos de framework hacia el nucleo.
 */
public final class PageMappings {

    private PageMappings() {
    }

    public static Pageable toPageable(PageQuery query, Set<String> allowedSortFields, String fallbackSort) {
        String field = query.sorted() && allowedSortFields.contains(query.sortBy()) ? query.sortBy() : fallbackSort;
        Sort.Direction direction = query.direction() == SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return PageRequest.of(query.page(), query.size(), Sort.by(direction, field));
    }

    public static <E, T> PageResult<T> toPageResult(Page<E> page, Function<E, T> mapper) {
        return new PageResult<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
