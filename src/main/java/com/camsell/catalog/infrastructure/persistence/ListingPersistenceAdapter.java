package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingAttribute;
import com.camsell.catalog.domain.model.ListingImage;
import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.catalog.domain.model.ListingStatus;
import com.camsell.catalog.domain.model.Money;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import com.camsell.shared.infrastructure.persistence.PageMappings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class ListingPersistenceAdapter implements ListingRepositoryPort {

    /** Campos por los que se permite ordenar desde la API publica. */
    private static final Set<String> SORTABLE = Set.of(
            "publishedAt", "priceAmount", "modelYear", "mileageKm", "viewCount", "createdAt");

    private final ListingJpaRepository repository;

    public ListingPersistenceAdapter(ListingJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Listing> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Listing> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public List<Listing> findAllByIds(List<Long> ids) {
        return repository.findByIdIn(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public PageResult<Listing> search(ListingSearchQuery query, PageQuery page) {
        return PageMappings.toPageResult(
                repository.findAll(
                        ListingSpecifications.from(query),
                        PageMappings.toPageable(page, SORTABLE, "publishedAt")),
                this::toDomain);
    }

    @Override
    public List<Listing> findFeatured(int limit) {
        return repository.findByFeaturedTrueAndStatusOrderByPublishedAtDesc(
                        ListingStatus.PUBLISHED,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public boolean existsByReference(String reference) {
        return repository.existsByReference(reference);
    }

    @Override
    public Listing save(Listing listing) {
        ListingEntity entity = listing.id() == null
                ? new ListingEntity()
                : repository.findById(listing.id()).orElseGet(ListingEntity::new);

        entity.setReference(listing.reference());
        entity.setSlug(listing.slug());
        entity.setSellerId(listing.sellerId());
        entity.setSellerName(listing.sellerName());
        entity.setBrandId(listing.brandId());
        entity.setBrandName(listing.brandName());
        entity.setModelId(listing.modelId());
        entity.setModelName(listing.modelName());
        entity.setTitle(listing.title());
        entity.setModelYear(listing.year());
        entity.setMileageKm(listing.mileageKm());
        entity.setPriceAmount(listing.price().amount());
        entity.setPriceCurrency(listing.price().currency());
        entity.setNegotiable(listing.negotiable());
        entity.setDescription(listing.description());
        entity.setCityCode(listing.cityCode());
        entity.setStatus(listing.status());
        entity.setFeatured(listing.featured());
        entity.setViewCount(listing.viewCount());
        entity.setPublishedAt(listing.publishedAt());

        entity.getImages().clear();
        listing.images().forEach(image -> {
            ListingImageEntity imageEntity = new ListingImageEntity();
            imageEntity.setUrl(image.url());
            imageEntity.setAltText(image.alt());
            imageEntity.setPosition(image.position());
            entity.getImages().add(imageEntity);
        });

        entity.getAttributes().clear();
        listing.attributes().forEach(attribute -> {
            ListingAttributeEntity attributeEntity = new ListingAttributeEntity();
            attributeEntity.setGroupCode(attribute.groupCode());
            attributeEntity.setOptionCode(attribute.optionCode());
            attributeEntity.setTextValue(attribute.textValue());
            attributeEntity.setNumberValue(attribute.numberValue());
            attributeEntity.setBoolValue(attribute.boolValue());
            entity.getAttributes().add(attributeEntity);
        });

        return toDomain(repository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        repository.incrementViewCount(id);
    }

    @Override
    public long countByStatus(ListingStatus status) {
        return repository.countByStatus(status);
    }

    @Override
    public long countBrandsWithListings() {
        return repository.countDistinctBrandsByStatus(ListingStatus.PUBLISHED);
    }

    private Listing toDomain(ListingEntity entity) {
        List<ListingImage> images = entity.getImages().stream()
                .map(image -> new ListingImage(
                        image.getId(), image.getUrl(), image.getAltText(), image.getPosition()))
                .toList();
        List<ListingAttribute> attributes = entity.getAttributes().stream()
                .map(attribute -> new ListingAttribute(
                        attribute.getId(),
                        attribute.getGroupCode(),
                        attribute.getOptionCode(),
                        attribute.getTextValue(),
                        attribute.getNumberValue(),
                        attribute.getBoolValue()))
                .toList();

        return new Listing(
                entity.getId(),
                entity.getReference(),
                entity.getSlug(),
                entity.getSellerId(),
                entity.getSellerName(),
                entity.getBrandId(),
                entity.getBrandName(),
                entity.getModelId(),
                entity.getModelName(),
                entity.getTitle(),
                entity.getModelYear(),
                entity.getMileageKm(),
                new Money(entity.getPriceAmount(), entity.getPriceCurrency()),
                entity.isNegotiable(),
                entity.getDescription(),
                entity.getCityCode(),
                entity.getStatus(),
                entity.isFeatured(),
                entity.getViewCount(),
                entity.getPublishedAt(),
                entity.getCreatedAt(),
                images,
                attributes);
    }
}
