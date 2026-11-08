package com.camsell.catalog.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;

import java.time.Instant;

/**
 * Oferta de compra sobre una publicacion. Es el lado "compra" del marketplace:
 * el comprador propone un precio y el vendedor la acepta o la rechaza.
 */
public record Offer(
        Long id,
        Long listingId,
        String listingReference,
        String listingTitle,
        Long sellerId,
        Long buyerId,
        String buyerName,
        String buyerPhone,
        Money amount,
        String message,
        OfferStatus status,
        Instant createdAt,
        Instant respondedAt) {

    public Offer transitionTo(OfferStatus newStatus, Instant now) {
        if (status.isFinal()) {
            throw new BusinessRuleException(
                    "offer_already_resolved",
                    "La oferta ya fue resuelta con estado " + status);
        }
        return new Offer(id, listingId, listingReference, listingTitle, sellerId, buyerId, buyerName,
                buyerPhone, amount, message, newStatus, createdAt, now);
    }

    public boolean isPending() {
        return status == OfferStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == OfferStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == OfferStatus.REJECTED;
    }
}
