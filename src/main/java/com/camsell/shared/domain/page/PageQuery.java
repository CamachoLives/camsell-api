package com.camsell.shared.domain.page;

/**
 * Peticion de paginacion expresada en el lenguaje del dominio, de forma que los
 * puertos de salida no dependan de Spring Data.
 */
public record PageQuery(int page, int size, String sortBy, SortDirection direction) {

    private static final int MAX_SIZE = 100;

    public PageQuery {
        page = Math.max(page, 0);
        size = size <= 0 ? 12 : Math.min(size, MAX_SIZE);
    }

    public static PageQuery of(Integer page, Integer size, String sortBy, String direction) {
        return new PageQuery(
                page == null ? 0 : page,
                size == null ? 12 : size,
                sortBy,
                SortDirection.fromOrNull(direction));
    }

    public boolean sorted() {
        return sortBy != null && !sortBy.isBlank();
    }
}
