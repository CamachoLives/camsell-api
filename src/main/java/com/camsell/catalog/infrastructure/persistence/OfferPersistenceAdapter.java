package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.model.Money;
import com.camsell.catalog.domain.model.Offer;
import com.camsell.catalog.domain.model.OfferStatus;
import com.camsell.catalog.domain.port.out.OfferRepositoryPort;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import com.camsell.shared.infrastructure.persistence.PageMappings;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class OfferPersistenceAdapter implements OfferRepositoryPort {

    private static final Set<String> SORTABLE = Set.of("createdAt", "amount", "status");

    private final OfferJpaRepository repository;

    public OfferPersistenceAdapter(OfferJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Offer save(Offer offer) {
        OfferEntity entity = offer.id() == null
                ? new OfferEntity()
                : repository.findById(offer.id()).orElseGet(OfferEntity::new);
        entity.setListingId(offer.listingId());
        entity.setListingReference(offer.listingReference());
        entity.setListingTitle(offer.listingTitle());
        entity.setSellerId(offer.sellerId());
        entity.setBuyerId(offer.buyerId());
        entity.setBuyerName(offer.buyerName());
        entity.setBuyerPhone(offer.buyerPhone());
        entity.setAmount(offer.amount().amount());
        entity.setCurrency(offer.amount().currency());
        entity.setMessage(offer.message());
        entity.setStatus(offer.status());
        entity.setRespondedAt(offer.respondedAt());
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<Offer> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Offer> findByListingId(Long listingId) {
        return repository.findByListingIdOrderByCreatedAtDesc(listingId).stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<Offer> findByBuyerId(Long buyerId, PageQuery page) {
        return PageMappings.toPageResult(
                repository.findByBuyerId(buyerId, PageMappings.toPageable(page, SORTABLE, "createdAt")),
                this::toDomain);
    }

    @Override
    public PageResult<Offer> findBySellerId(Long sellerId, PageQuery page) {
        return PageMappings.toPageResult(
                repository.findBySellerId(sellerId, PageMappings.toPageable(page, SORTABLE, "createdAt")),
                this::toDomain);
    }

    @Override
    public boolean existsPendingByListingAndBuyer(Long listingId, Long buyerId) {
        return repository.existsByListingIdAndBuyerIdAndStatus(listingId, buyerId, OfferStatus.PENDING);
    }

    @Override
    public long countPendingBySeller(Long sellerId) {
        return repository.countBySellerIdAndStatus(sellerId, OfferStatus.PENDING);
    }

    @Override
    public void rejectOtherPendingOffers(Long listingId, Long acceptedOfferId) {
        repository.rejectOtherPendingOffers(listingId, acceptedOfferId, Instant.now());
    }

    private Offer toDomain(OfferEntity entity) {
        return new Offer(
                entity.getId(),
                entity.getListingId(),
                entity.getListingReference(),
                entity.getListingTitle(),
                entity.getSellerId(),
                entity.getBuyerId(),
                entity.getBuyerName(),
                entity.getBuyerPhone(),
                new Money(entity.getAmount(), entity.getCurrency()),
                entity.getMessage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getRespondedAt());
    }
}
