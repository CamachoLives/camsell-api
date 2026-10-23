package com.camsell.catalog.domain.model;

import java.time.Instant;

/**
 * Proyeccion de lectura para listados: exactamente lo que necesita una tarjeta
 * del catalogo, sin las imagenes adicionales ni los atributos del vehiculo.
 *
 * <p>Existe para no cargar el agregado completo en una busqueda. Mapear
 * {@link Listing} por cada resultado obliga a resolver sus dos colecciones
 * perezosas, lo que convierte una pagina de doce publicaciones en veinticinco
 * consultas. La tarjeta solo toca la portada, de modo que la pagina se resuelve
 * en dos.
 */
public record ListingCard(
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
        Money price,
        boolean negotiable,
        String cityCode,
        ListingStatus status,
        boolean featured,
        long viewCount,
        String coverImageUrl,
        Instant publishedAt) {
}
