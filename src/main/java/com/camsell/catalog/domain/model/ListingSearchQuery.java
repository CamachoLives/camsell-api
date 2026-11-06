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

    /** Indica si la consulta incluye un filtro de texto libre. */
    public boolean hasText() {
        return text != null && !text.isBlank();
    }

    /** Indica si la consulta incluye al menos un filtro por atributo parametrizado. */
    public boolean hasAttributeFilters() {
        return !attributeFilters.isEmpty();
    }

    /** Indica si la consulta esta sin ningun criterio de acotamiento (devuelve todo el catalogo). */
    public boolean isUnfiltered() {
        return !hasText()
                && brandIds.isEmpty()
                && modelIds.isEmpty()
                && minYear == null
                && maxYear == null
                && minPrice == null
                && maxPrice == null
                && maxMileage == null
                && cityCode == null
                && negotiable == null
                && featured == null
                && !hasAttributeFilters();
    }
}
