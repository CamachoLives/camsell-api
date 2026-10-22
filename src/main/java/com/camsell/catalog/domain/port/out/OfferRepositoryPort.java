package com.camsell.catalog.domain.port.out;

import com.camsell.catalog.domain.model.Offer;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;

import java.util.List;
import java.util.Optional;

public interface OfferRepositoryPort {

    Offer save(Offer offer);

    Optional<Offer> findById(Long id);

    List<Offer> findByListingId(Long listingId);

    PageResult<Offer> findByBuyerId(Long buyerId, PageQuery page);

    PageResult<Offer> findBySellerId(Long sellerId, PageQuery page);

    boolean existsPendingByListingAndBuyer(Long listingId, Long buyerId);

    long countPendingBySeller(Long sellerId);

    void rejectOtherPendingOffers(Long listingId, Long acceptedOfferId);
}
