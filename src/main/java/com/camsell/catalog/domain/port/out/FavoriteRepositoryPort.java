package com.camsell.catalog.domain.port.out;

import java.util.List;

public interface FavoriteRepositoryPort {

    void add(Long userId, Long listingId);

    void remove(Long userId, Long listingId);

    boolean exists(Long userId, Long listingId);

    List<Long> findListingIdsByUser(Long userId);

    long countByListing(Long listingId);
}
