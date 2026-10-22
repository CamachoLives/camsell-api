package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.port.out.FavoriteRepositoryPort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class FavoritePersistenceAdapter implements FavoriteRepositoryPort {

    private final FavoriteJpaRepository repository;

    public FavoritePersistenceAdapter(FavoriteJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void add(Long userId, Long listingId) {
        if (repository.existsByUserIdAndListingId(userId, listingId)) {
            return;
        }
        FavoriteEntity entity = new FavoriteEntity();
        entity.setUserId(userId);
        entity.setListingId(listingId);
        entity.setCreatedAt(Instant.now());
        repository.save(entity);
    }

    @Override
    public void remove(Long userId, Long listingId) {
        repository.deleteByUserIdAndListingId(userId, listingId);
    }

    @Override
    public boolean exists(Long userId, Long listingId) {
        return repository.existsByUserIdAndListingId(userId, listingId);
    }

    @Override
    public List<Long> findListingIdsByUser(Long userId) {
        return repository.findListingIdsByUserId(userId);
    }

    @Override
    public long countByListing(Long listingId) {
        return repository.countByListingId(listingId);
    }
}
