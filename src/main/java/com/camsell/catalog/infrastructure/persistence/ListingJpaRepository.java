package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.model.ListingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ListingJpaRepository
        extends JpaRepository<ListingEntity, Long>, JpaSpecificationExecutor<ListingEntity> {

    Optional<ListingEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByReference(String reference);

    List<ListingEntity> findByIdIn(Collection<Long> ids);

    List<ListingEntity> findByFeaturedTrueAndStatusOrderByPublishedAtDesc(ListingStatus status, Pageable pageable);

    long countByStatus(ListingStatus status);

    @Query("select count(distinct l.brandId) from ListingEntity l where l.status = :status")
    long countDistinctBrandsByStatus(@Param("status") ListingStatus status);

    @Modifying
    @Query("update ListingEntity l set l.viewCount = l.viewCount + 1 where l.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
