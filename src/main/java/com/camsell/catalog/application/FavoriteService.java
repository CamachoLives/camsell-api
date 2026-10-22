package com.camsell.catalog.application;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.port.in.FavoriteUseCase;
import com.camsell.catalog.domain.port.out.FavoriteRepositoryPort;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FavoriteService implements FavoriteUseCase {

    private final FavoriteRepositoryPort favorites;
    private final ListingRepositoryPort listings;
    private final CurrentUserPort currentUser;

    public FavoriteService(
            FavoriteRepositoryPort favorites,
            ListingRepositoryPort listings,
            CurrentUserPort currentUser) {
        this.favorites = favorites;
        this.listings = listings;
        this.currentUser = currentUser;
    }

    @Override
    public boolean toggle(Long listingId) {
        Long userId = currentUser.requireCurrentUserId();
        if (listings.findById(listingId).isEmpty()) {
            throw new ResourceNotFoundException("Publicacion", listingId);
        }
        if (favorites.exists(userId, listingId)) {
            favorites.remove(userId, listingId);
            return false;
        }
        favorites.add(userId, listingId);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Listing> listMine() {
        List<Long> ids = favorites.findListingIdsByUser(currentUser.requireCurrentUserId());
        return ids.isEmpty() ? List.of() : listings.findAllByIds(ids);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorite(Long listingId) {
        return currentUser.currentUserId()
                .map(userId -> favorites.exists(userId, listingId))
                .orElse(false);
    }
}
