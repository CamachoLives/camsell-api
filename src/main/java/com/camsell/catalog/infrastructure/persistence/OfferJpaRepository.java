package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.model.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OfferJpaRepository extends JpaRepository<OfferEntity, Long> {

    List<OfferEntity> findByListingIdOrderByCreatedAtDesc(Long listingId);

    Page<OfferEntity> findByBuyerId(Long buyerId, Pageable pageable);

    Page<OfferEntity> findBySellerId(Long sellerId, Pageable pageable);

    boolean existsByListingIdAndBuyerIdAndStatus(Long listingId, Long buyerId, OfferStatus status);

    long countBySellerIdAndStatus(Long sellerId, OfferStatus status);

    @Modifying
    @Query("""
            update OfferEntity o
               set o.status = com.camsell.catalog.domain.model.OfferStatus.REJECTED,
                   o.respondedAt = :now
             where o.listingId = :listingId
               and o.id <> :acceptedId
               and o.status = com.camsell.catalog.domain.model.OfferStatus.PENDING
            """)
    void rejectOtherPendingOffers(
            @Param("listingId") Long listingId,
            @Param("acceptedId") Long acceptedId,
            @Param("now") Instant now);
}
