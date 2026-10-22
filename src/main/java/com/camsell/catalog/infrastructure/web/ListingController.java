package com.camsell.catalog.infrastructure.web;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.catalog.domain.port.in.FavoriteUseCase;
import com.camsell.catalog.domain.port.in.ListingQueryUseCase;
import com.camsell.catalog.domain.port.out.FavoriteRepositoryPort;
import com.camsell.catalog.infrastructure.web.dto.ListingResponses;
import com.camsell.shared.api.dto.PageResponse;
import com.camsell.shared.domain.page.PageQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Catalogo publico de vehiculos.
 *
 * <p>Los filtros por parametros se envian con el parametro repetible
 * {@code attr} en formato {@code GRUPO:OPCION}, por ejemplo
 * {@code ?attr=FUEL_TYPE:DIESEL&attr=TRANSMISSION:AUTOMATIC}. Gracias a esto un
 * grupo de parametros nuevo se vuelve filtrable sin cambiar el contrato.
 */
@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingQueryUseCase listings;
    private final FavoriteUseCase favorites;
    private final FavoriteRepositoryPort favoriteCounts;

    public ListingController(
            ListingQueryUseCase listings,
            FavoriteUseCase favorites,
            FavoriteRepositoryPort favoriteCounts) {
        this.listings = listings;
        this.favorites = favorites;
        this.favoriteCounts = favoriteCounts;
    }

    @GetMapping
    public PageResponse<ListingResponses.Summary> search(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> brandIds,
            @RequestParam(required = false) List<Long> modelIds,
            @RequestParam(required = false) Integer minYear,
            @RequestParam(required = false) Integer maxYear,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer maxMileage,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) Boolean negotiable,
            @RequestParam(required = false) Boolean featured,
            @RequestParam(required = false) List<String> attr,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {

        ListingSearchQuery query = new ListingSearchQuery(
                text,
                brandIds,
                modelIds,
                minYear,
                maxYear,
                minPrice,
                maxPrice,
                maxMileage,
                cityCode,
                Set.of(ListingStatus.PUBLISHED, ListingStatus.RESERVED),
                null,
                featured,
                negotiable,
                parseAttributeFilters(attr));

        return PageResponse.from(listings
                .search(query, PageQuery.of(page, size, sort, direction))
                .map(ListingResponses.Summary::from));
    }

    @GetMapping("/featured")
    public List<ListingResponses.Summary> featured(@RequestParam(defaultValue = "8") int limit) {
        return listings.featured(limit).stream().map(ListingResponses.Summary::from).toList();
    }

    @GetMapping("/{slug}")
    public ListingResponses.Detail detail(@PathVariable String slug) {
        Listing listing = listings.getBySlug(slug);
        return ListingResponses.Detail.from(
                listing,
                favorites.isFavorite(listing.id()),
                favoriteCounts.countByListing(listing.id()));
    }

    /** Convierte {@code GRUPO:OPCION} repetido en el mapa de filtros del dominio. */
    static Map<String, List<String>> parseAttributeFilters(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> filters = new HashMap<>();
        for (String entry : raw) {
            if (entry == null || !entry.contains(":")) {
                continue;
            }
            int separator = entry.indexOf(':');
            String group = entry.substring(0, separator).trim();
            String option = entry.substring(separator + 1).trim();
            if (group.isEmpty() || option.isEmpty()) {
                continue;
            }
            filters.computeIfAbsent(group, key -> new ArrayList<>()).add(option);
        }
        return filters;
    }
}
