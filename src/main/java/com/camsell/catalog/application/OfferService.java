package com.camsell.catalog.application;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.catalog.domain.model.Money;
import com.camsell.catalog.domain.model.Offer;
import com.camsell.catalog.domain.model.OfferStatus;
import com.camsell.catalog.domain.port.in.OfferUseCase;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.catalog.domain.port.out.OfferRepositoryPort;
import com.camsell.catalog.domain.port.out.SellerLookupPort;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.shared.domain.exception.BusinessRuleException;
import com.camsell.shared.domain.exception.OperationNotAllowedException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Flujo de compra: ofertas del comprador y respuesta del vendedor. */
@Service
@Transactional
public class OfferService implements OfferUseCase {

    private static final Logger log = LoggerFactory.getLogger(OfferService.class);
    private static final BigDecimal MIN_RATIO = new BigDecimal("40");

    private final OfferRepositoryPort offers;
    private final ListingRepositoryPort listings;
    private final CurrentUserPort currentUser;
    private final SellerLookupPort users;

    public OfferService(
            OfferRepositoryPort offers,
            ListingRepositoryPort listings,
            CurrentUserPort currentUser,
            SellerLookupPort users) {
        this.offers = offers;
        this.listings = listings;
        this.currentUser = currentUser;
        this.users = users;
    }

    @Override
    public Offer submit(Long listingId, SubmitOfferCommand command) {
        Long buyerId = currentUser.requireCurrentUserId();
        Listing listing = listings.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", listingId));

        if (listing.sellerId().equals(buyerId)) {
            throw new BusinessRuleException(
                    "self_offer_not_allowed",
                    "No puedes ofertar sobre tu propia publicacion");
        }
        if (!listing.status().acceptsOffers()) {
            throw new BusinessRuleException(
                    "listing_not_open",
                    "La publicacion no esta recibiendo ofertas (estado " + listing.status() + ")");
        }
        if (offers.existsPendingByListingAndBuyer(listingId, buyerId)) {
            throw new BusinessRuleException(
                    "duplicated_offer",
                    "Ya tienes una oferta pendiente sobre esta publicacion");
        }

        Money amount = new Money(command.amount(), command.currency() == null
                ? listing.price().currency()
                : command.currency());
        if (amount.percentageOf(listing.price()).compareTo(MIN_RATIO) < 0) {
            throw new BusinessRuleException(
                    "offer_too_low",
                    "La oferta debe ser al menos el 40% del precio publicado");
        }

        SellerLookupPort.SellerInfo buyer = users.findSeller(buyerId);
        Offer offer = new Offer(
                null,
                listing.id(),
                listing.reference(),
                listing.title(),
                listing.sellerId(),
                buyerId,
                buyer.fullName(),
                command.contactPhone() == null ? buyer.phone() : command.contactPhone(),
                amount,
                command.message(),
                OfferStatus.PENDING,
                null,
                null);
        Offer saved = offers.save(offer);
        log.info("Oferta {} creada sobre {} por usuario {}", saved.id(), listing.reference(), buyerId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> listByListing(Long listingId) {
        Listing listing = listings.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Publicacion", listingId));
        if (!currentUser.canManage(listing.sellerId())) {
            throw new OperationNotAllowedException("Solo el vendedor puede ver las ofertas de la publicacion");
        }
        return offers.findByListingId(listingId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Offer> listMineAsBuyer(PageQuery page) {
        return offers.findByBuyerId(currentUser.requireCurrentUserId(), page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Offer> listMineAsSeller(PageQuery page) {
        return offers.findBySellerId(currentUser.requireCurrentUserId(), page);
    }

    @Override
    public Offer accept(Long offerId) {
        Offer offer = requireSellerAccess(offerId);
        Offer accepted = offers.save(offer.transitionTo(OfferStatus.ACCEPTED, Instant.now()));
        offers.rejectOtherPendingOffers(offer.listingId(), offer.id());

        // Aceptar una oferta reserva el vehiculo para cerrar el negocio.
        listings.findById(offer.listingId())
                .map(listing -> listing.withStatus(ListingStatus.RESERVED, Instant.now()))
                .ifPresent(listings::save);
        log.info("Oferta {} aceptada; publicacion {} reservada", offerId, offer.listingReference());
        return accepted;
    }

    @Override
    public Offer reject(Long offerId) {
        Offer offer = requireSellerAccess(offerId);
        return offers.save(offer.transitionTo(OfferStatus.REJECTED, Instant.now()));
    }

    @Override
    public Offer withdraw(Long offerId) {
        Offer offer = offers.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta", offerId));
        if (!offer.buyerId().equals(currentUser.requireCurrentUserId())) {
            throw new OperationNotAllowedException("Solo el comprador puede retirar su oferta");
        }
        return offers.save(offer.transitionTo(OfferStatus.WITHDRAWN, Instant.now()));
    }

    @Override
    @Transactional(readOnly = true)
    public long pendingCountForCurrentSeller() {
        return offers.countPendingBySeller(currentUser.requireCurrentUserId());
    }

    private Offer requireSellerAccess(Long offerId) {
        Offer offer = offers.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta", offerId));
        if (!currentUser.canManage(offer.sellerId())) {
            throw new OperationNotAllowedException("Solo el vendedor puede responder esta oferta");
        }
        return offer;
    }
}
