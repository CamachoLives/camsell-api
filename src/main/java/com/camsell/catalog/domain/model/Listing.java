package com.camsell.catalog.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;

import java.time.Instant;
import java.time.Year;
import java.util.List;

/**
 * Agregado raiz del catalogo: una publicacion de venta de vehiculo. Concentra
 * las reglas de publicacion y de cambio de estado.
 */
public record Listing(
        Long id,
        String reference,
        String slug,
        Long sellerId,
        String sellerName,
        Long brandId,
        String brandName,
        Long modelId,
        String modelName,
        String title,
        int year,
        int mileageKm,
        Money price,
        boolean negotiable,
        String description,
        String cityCode,
        ListingStatus status,
        boolean featured,
        long viewCount,
        Instant publishedAt,
        Instant createdAt,
        List<ListingImage> images,
        List<ListingAttribute> attributes) {

    public Listing {
        images = images == null ? List.of() : List.copyOf(images);
        attributes = attributes == null ? List.of() : List.copyOf(attributes);
    }

    /**
     * Reglas que debe cumplir una publicacion antes de ser visible al publico.
     *
     * @param maxImages limite configurable de imagenes por publicacion
     * @param minYear   ano minimo aceptado por la plataforma
     */
    public void validateForPublication(int maxImages, int minYear) {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("El titulo de la publicacion es obligatorio");
        }
        if (images.isEmpty()) {
            throw new BusinessRuleException(
                    "listing_without_images",
                    "Una publicacion debe tener al menos una imagen para salir al aire");
        }
        if (images.size() > maxImages) {
            throw new BusinessRuleException(
                    "too_many_images",
                    "Maximo %d imagenes por publicacion".formatted(maxImages));
        }
        int currentYear = Year.now().getValue();
        if (year < minYear || year > currentYear + 1) {
            throw new BusinessRuleException(
                    "invalid_year",
                    "El ano debe estar entre %d y %d".formatted(minYear, currentYear + 1));
        }
        if (mileageKm < 0) {
            throw new BusinessRuleException("El kilometraje no puede ser negativo");
        }
        if (description == null || description.length() < 40) {
            throw new BusinessRuleException(
                    "short_description",
                    "La descripcion debe tener al menos 40 caracteres");
        }
    }

    public Listing withStatus(ListingStatus newStatus, Instant publishedTimestamp) {
        if (status == ListingStatus.SOLD && newStatus != ListingStatus.ARCHIVED) {
            throw new BusinessRuleException(
                    "listing_already_sold",
                    "Una publicacion vendida ya no admite cambios de estado");
        }
        Instant published = newStatus == ListingStatus.PUBLISHED && publishedAt == null
                ? publishedTimestamp
                : publishedAt;
        return new Listing(id, reference, slug, sellerId, sellerName, brandId, brandName, modelId, modelName,
                title, year, mileageKm, price, negotiable, description, cityCode, newStatus, featured,
                viewCount, published, createdAt, images, attributes);
    }

    public Listing withFeatured(boolean newFeatured) {
        return new Listing(id, reference, slug, sellerId, sellerName, brandId, brandName, modelId, modelName,
                title, year, mileageKm, price, negotiable, description, cityCode, status, newFeatured,
                viewCount, publishedAt, createdAt, images, attributes);
    }

    public Listing withOwnership(Long ownerId, String ownerName) {
        return new Listing(id, reference, slug, ownerId, ownerName, brandId, brandName, modelId, modelName,
                title, year, mileageKm, price, negotiable, description, cityCode, status, featured,
                viewCount, publishedAt, createdAt, images, attributes);
    }

    public ListingImage coverImage() {
        return images.stream()
                .min((a, b) -> Integer.compare(a.position(), b.position()))
                .orElse(null);
    }

    public String attributeValue(String groupCode) {
        return attributes.stream()
                .filter(attribute -> attribute.groupCode().equals(groupCode))
                .map(ListingAttribute::displayValue)
                .findFirst()
                .orElse(null);
    }
}
