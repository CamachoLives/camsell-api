package com.camsell.catalog.infrastructure.web.dto;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Proyecciones de lectura del catalogo. */
public final class ListingResponses {

    private ListingResponses() {
    }

    /** Tarjeta usada en resultados de busqueda y carruseles. */
    public record Summary(
            Long id,
            String reference,
            String slug,
            String title,
            Long brandId,
            String brandName,
            Long modelId,
            String modelName,
            int year,
            int mileageKm,
            BigDecimal price,
            String currency,
            boolean negotiable,
            String cityCode,
            ListingStatus status,
            boolean featured,
            long viewCount,
            String coverImageUrl,
            String sellerName,
            Instant publishedAt) {

        public static Summary from(Listing listing) {
            return new Summary(
                    listing.id(),
                    listing.reference(),
                    listing.slug(),
                    listing.title(),
                    listing.brandId(),
                    listing.brandName(),
                    listing.modelId(),
                    listing.modelName(),
                    listing.year(),
                    listing.mileageKm(),
                    listing.price().amount(),
                    listing.price().currency(),
                    listing.negotiable(),
                    listing.cityCode(),
                    listing.status(),
                    listing.featured(),
                    listing.viewCount(),
                    listing.coverImage() == null ? null : listing.coverImage().url(),
                    listing.sellerName(),
                    listing.publishedAt());
        }
    }

    public record Image(Long id, String url, String alt, int position) {
    }

    public record Attribute(
            String groupCode,
            String optionCode,
            String textValue,
            BigDecimal numberValue,
            Boolean boolValue,
            String displayValue) {
    }

    /** Ficha completa del vehiculo. */
    public record Detail(
            Long id,
            String reference,
            String slug,
            String title,
            Long sellerId,
            String sellerName,
            Long brandId,
            String brandName,
            Long modelId,
            String modelName,
            int year,
            int mileageKm,
            BigDecimal price,
            String currency,
            boolean negotiable,
            String description,
            String cityCode,
            ListingStatus status,
            boolean featured,
            long viewCount,
            boolean favorite,
            long favoriteCount,
            Instant publishedAt,
            Instant createdAt,
            List<Image> images,
            List<Attribute> attributes) {

        public static Detail from(Listing listing, boolean favorite, long favoriteCount) {
            return new Detail(
                    listing.id(),
                    listing.reference(),
                    listing.slug(),
                    listing.title(),
                    listing.sellerId(),
                    listing.sellerName(),
                    listing.brandId(),
                    listing.brandName(),
                    listing.modelId(),
                    listing.modelName(),
                    listing.year(),
                    listing.mileageKm(),
                    listing.price().amount(),
                    listing.price().currency(),
                    listing.negotiable(),
                    listing.description(),
                    listing.cityCode(),
                    listing.status(),
                    listing.featured(),
                    listing.viewCount(),
                    favorite,
                    favoriteCount,
                    listing.publishedAt(),
                    listing.createdAt(),
                    listing.images().stream()
                            .map(image -> new Image(image.id(), image.url(), image.alt(), image.position()))
                            .toList(),
                    listing.attributes().stream()
                            .map(attribute -> new Attribute(
                                    attribute.groupCode(),
                                    attribute.optionCode(),
                                    attribute.textValue(),
                                    attribute.numberValue(),
                                    attribute.boolValue(),
                                    attribute.displayValue()))
                            .toList());
        }
    }
}
