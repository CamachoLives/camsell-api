package com.camsell.catalog.infrastructure.persistence;

import com.camsell.catalog.domain.model.ListingSearchQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Construye la consulta del catalogo con la Criteria API. Los filtros por
 * parametros se resuelven con subconsultas EXISTS, de modo que un grupo nuevo
 * se vuelve filtrable sin tocar esta clase.
 */
final class ListingSpecifications {

    private ListingSpecifications() {
    }

    static Specification<ListingEntity> from(ListingSearchQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(root.get("status").in(query.statuses()));

            if (query.text() != null && !query.text().isBlank()) {
                String pattern = "%" + query.text().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("brandName")), pattern),
                        cb.like(cb.lower(root.get("modelName")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("reference")), pattern)));
            }
            if (!query.brandIds().isEmpty()) {
                predicates.add(root.get("brandId").in(query.brandIds()));
            }
            if (!query.modelIds().isEmpty()) {
                predicates.add(root.get("modelId").in(query.modelIds()));
            }
            if (query.minYear() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("modelYear"), query.minYear()));
            }
            if (query.maxYear() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("modelYear"), query.maxYear()));
            }
            if (query.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priceAmount"), query.minPrice()));
            }
            if (query.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priceAmount"), query.maxPrice()));
            }
            if (query.maxMileage() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("mileageKm"), query.maxMileage()));
            }
            if (query.cityCode() != null && !query.cityCode().isBlank()) {
                predicates.add(cb.equal(root.get("cityCode"), query.cityCode()));
            }
            if (query.sellerId() != null) {
                predicates.add(cb.equal(root.get("sellerId"), query.sellerId()));
            }
            if (query.featured() != null) {
                predicates.add(cb.equal(root.get("featured"), query.featured()));
            }
            if (query.negotiable() != null) {
                predicates.add(cb.equal(root.get("negotiable"), query.negotiable()));
            }

            for (Map.Entry<String, List<String>> filter : query.attributeFilters().entrySet()) {
                List<String> values = filter.getValue();
                if (values == null || values.isEmpty()) {
                    continue;
                }
                Subquery<Long> subquery = criteriaQuery.subquery(Long.class);
                Root<ListingAttributeEntity> attribute = subquery.from(ListingAttributeEntity.class);
                subquery.select(attribute.get("listingId"))
                        .where(cb.and(
                                cb.equal(attribute.get("listingId"), root.get("id")),
                                cb.equal(attribute.get("groupCode"), filter.getKey()),
                                attribute.get("optionCode").in(values)));
                predicates.add(cb.exists(subquery));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
