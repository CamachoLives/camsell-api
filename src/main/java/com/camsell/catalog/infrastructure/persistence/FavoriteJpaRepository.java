package com.camsell.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteJpaRepository extends JpaRepository<FavoriteEntity, Long> {

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    void deleteByUserIdAndListingId(Long userId, Long listingId);

    @Query("select f.listingId from FavoriteEntity f where f.userId = :userId order by f.createdAt desc")
    List<Long> findListingIdsByUserId(@Param("userId") Long userId);

    long countByListingId(Long listingId);
}
