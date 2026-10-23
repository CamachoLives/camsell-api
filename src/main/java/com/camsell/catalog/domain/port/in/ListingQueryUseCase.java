package com.camsell.catalog.domain.port.in;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingCard;
import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;

import java.util.List;

public interface ListingQueryUseCase {

    PageResult<ListingCard> search(ListingSearchQuery query, PageQuery page);

    /** Ficha publica por slug; registra la visita. */
    Listing getBySlug(String slug);

    Listing getById(Long id);

    List<ListingCard> featured(int limit);

    PageResult<ListingCard> listOwnedByCurrentUser(ListingSearchQuery query, PageQuery page);

    CatalogStats stats();

    /** Resumen usado por la portada del sitio. */
    record CatalogStats(
            long publishedListings,
            long soldListings,
            long reservedListings,
            long brandsWithListings) {
    }
}
