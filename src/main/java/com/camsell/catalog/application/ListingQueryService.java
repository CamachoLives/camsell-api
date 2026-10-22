package com.camsell.catalog.application;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.catalog.domain.port.in.ListingQueryUseCase;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.shared.config.CamsellProperties;
import com.camsell.shared.domain.exception.OperationNotAllowedException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ListingQueryService implements ListingQueryUseCase {

    private final ListingRepositoryPort listings;
    private final CurrentUserPort currentUser;
    private final CamsellProperties properties;

    public ListingQueryService(
            ListingRepositoryPort listings,
            CurrentUserPort currentUser,
            CamsellProperties properties) {
        this.listings = listings;
        this.currentUser = currentUser;
        this.properties = properties;
    }

    @Override
    public PageResult<Listing> search(ListingSearchQuery query, PageQuery page) {
        return listings.search(query, page);
    }

    @Override
    @Transactional
    public Listing getBySlug(String slug) {
        Listing listing = listings.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", slug));
        if (!listing.status().isPubliclyVisible() && !currentUser.canManage(listing.sellerId())) {
            throw new ResourceNotFoundException("Publicacion", slug);
        }
        listings.incrementViewCount(listing.id());
        return listing;
    }

    @Override
    public Listing getById(Long id) {
        Listing listing = listings.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", id));
        if (!listing.status().isPubliclyVisible() && !currentUser.canManage(listing.sellerId())) {
            throw new OperationNotAllowedException("No tienes acceso a esta publicacion");
        }
        return listing;
    }

    @Override
    public List<Listing> featured(int limit) {
        int max = properties.catalog().maxFeaturedListings();
        return listings.findFeatured(limit <= 0 ? max : Math.min(limit, max));
    }

    @Override
    public PageResult<Listing> listOwnedByCurrentUser(ListingSearchQuery query, PageQuery page) {
        Long sellerId = currentUser.requireCurrentUserId();
        ListingSearchQuery scoped = query.restrictedToSeller(sellerId, EnumSet.allOf(ListingStatus.class));
        return listings.search(scoped, page);
    }

    @Override
    public CatalogStats stats() {
        return new CatalogStats(
                listings.countByStatus(ListingStatus.PUBLISHED),
                listings.countByStatus(ListingStatus.SOLD),
                listings.countByStatus(ListingStatus.RESERVED),
                listings.countBrandsWithListings());
    }
}
