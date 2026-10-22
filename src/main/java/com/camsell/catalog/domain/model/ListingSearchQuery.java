package com.camsell.catalog.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Criterios de busqueda del catalogo. {@code attributeFilters} lleva pares
 * {@code GRUPO -> [OPCIONES]} resueltos a partir del catalogo de parametros, de
 * forma que un filtro nuevo no requiere cambios en el codigo de busqueda.
 */
public record ListingSearchQuery(
        String text,
        List<Long> brandIds,
        List<Long> modelIds,
        Integer minYear,
        Integer maxYear,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer maxMileage,
        String cityCode,
        Set<ListingStatus> statuses,
        Long sellerId,
        Boolean featured,
        Boolean negotiable,
        Map<String, List<String>> attributeFilters) {

    public ListingSearchQuery {
        brandIds = brandIds == null ? List.of() : List.copyOf(brandIds);
        modelIds = modelIds == null ? List.of() : List.copyOf(modelIds);
        statuses = statuses == null || statuses.isEmpty()
                ? Set.of(ListingStatus.PUBLISHED, ListingStatus.RESERVED)
                : Set.copyOf(statuses);
        attributeFilters = attributeFilters == null ? Map.of() : Map.copyOf(attributeFilters);
    }

    public static ListingSearchQuery publicDefaults() {
        return new ListingSearchQuery(null, null, null, null, null, null, null, null, null,
                null, null, null, null, null);
    }

    public ListingSearchQuery restrictedToSeller(Long id, Set<ListingStatus> allowedStatuses) {
        return new ListingSearchQuery(text, brandIds, modelIds, minYear, maxYear, minPrice, maxPrice,
                maxMileage, cityCode, allowedStatuses, id, featured, negotiable, attributeFilters);
    }
}
