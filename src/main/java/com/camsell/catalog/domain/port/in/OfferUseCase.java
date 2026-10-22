package com.camsell.catalog.domain.port.in;

import com.camsell.catalog.domain.model.Offer;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface OfferUseCase {

    Offer submit(Long listingId, SubmitOfferCommand command);

    List<Offer> listByListing(Long listingId);

    PageResult<Offer> listMineAsBuyer(PageQuery page);

    PageResult<Offer> listMineAsSeller(PageQuery page);

    Offer accept(Long offerId);

    Offer reject(Long offerId);

    Offer withdraw(Long offerId);

    long pendingCountForCurrentSeller();

    record SubmitOfferCommand(BigDecimal amount, String currency, String message, String contactPhone) {
    }
}
