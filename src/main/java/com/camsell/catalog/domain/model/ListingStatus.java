package com.camsell.catalog.domain.model;

import java.util.Set;

/** Ciclo de vida de una publicacion de venta. */
public enum ListingStatus {
    /** Creada por el vendedor pero aun no visible al publico. */
    DRAFT,
    /** Visible en el catalogo y en los resultados de busqueda. */
    PUBLISHED,
    /** Con una oferta aceptada, pendiente de cierre. */
    RESERVED,
    /** Vendida. */
    SOLD,
    /** Retirada del catalogo por el vendedor o por moderacion. */
    ARCHIVED;

    private static final Set<ListingStatus> VISIBLE = Set.of(PUBLISHED, RESERVED);
    private static final Set<ListingStatus> TERMINAL = Set.of(SOLD, ARCHIVED);

    public boolean isPubliclyVisible() {
        return VISIBLE.contains(this);
    }

    public boolean acceptsOffers() {
        return this == PUBLISHED;
    }

    /**
     * Estados que ya no admiten mas transiciones de negocio.
     * Analogia directa con {@code OfferStatus.isFinal()}.
     */
    public boolean isFinal() {
        return TERMINAL.contains(this);
    }
}
