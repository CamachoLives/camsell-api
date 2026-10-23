package com.camsell.catalog.domain.port.out;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingCard;
import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;

import java.util.List;
import java.util.Optional;

public interface ListingRepositoryPort {

    Optional<Listing> findById(Long id);

    Optional<Listing> findBySlug(String slug);

    List<ListingCard> findCardsByIds(List<Long> ids);

    /** Busqueda paginada que devuelve solo lo necesario para las tarjetas. */
    PageResult<ListingCard> search(ListingSearchQuery query, PageQuery page);

    List<ListingCard> findFeatured(int limit);

    boolean existsBySlug(String slug);

    boolean existsByReference(String reference);

    Listing save(Listing listing);

    void deleteById(Long id);

    void incrementViewCount(Long id);

    long countByStatus(ListingStatus status);

    long countBrandsWithListings();
}
