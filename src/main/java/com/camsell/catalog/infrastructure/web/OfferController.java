package com.camsell.catalog.infrastructure.web;

import com.camsell.catalog.domain.port.in.OfferUseCase;
import com.camsell.catalog.infrastructure.web.dto.OfferDtos;
import com.camsell.shared.api.dto.PageResponse;
import com.camsell.shared.domain.page.PageQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Flujo de compra: ofertas enviadas y recibidas. */
@RestController
@RequestMapping("/api/v1")
public class OfferController {

    private final OfferUseCase offers;

    public OfferController(OfferUseCase offers) {
        this.offers = offers;
    }

    @PostMapping("/listings/{listingId}/offers")
    @ResponseStatus(HttpStatus.CREATED)
    public OfferDtos.Response submit(
            @PathVariable Long listingId,
            @Valid @RequestBody OfferDtos.Submit payload) {
        return OfferDtos.Response.from(offers.submit(listingId, new OfferUseCase.SubmitOfferCommand(
                payload.amount(), payload.currency(), payload.message(), payload.contactPhone())));
    }

    @GetMapping("/listings/{listingId}/offers")
    public List<OfferDtos.Response> byListing(@PathVariable Long listingId) {
        return offers.listByListing(listingId).stream().map(OfferDtos.Response::from).toList();
    }

    @GetMapping("/offers/sent")
    public PageResponse<OfferDtos.Response> sent(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return PageResponse.from(offers
                .listMineAsBuyer(PageQuery.of(page, size, "createdAt", "desc"))
                .map(OfferDtos.Response::from));
    }

    @GetMapping("/offers/received")
    public PageResponse<OfferDtos.Response> received(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return PageResponse.from(offers
                .listMineAsSeller(PageQuery.of(page, size, "createdAt", "desc"))
                .map(OfferDtos.Response::from));
    }

    @GetMapping("/offers/pending-count")
    public long pendingCount() {
        return offers.pendingCountForCurrentSeller();
    }

    @PostMapping("/offers/{offerId}/accept")
    public OfferDtos.Response accept(@PathVariable Long offerId) {
        return OfferDtos.Response.from(offers.accept(offerId));
    }

    @PostMapping("/offers/{offerId}/reject")
    public OfferDtos.Response reject(@PathVariable Long offerId) {
        return OfferDtos.Response.from(offers.reject(offerId));
    }

    @PostMapping("/offers/{offerId}/withdraw")
    public OfferDtos.Response withdraw(@PathVariable Long offerId) {
        return OfferDtos.Response.from(offers.withdraw(offerId));
    }
}
