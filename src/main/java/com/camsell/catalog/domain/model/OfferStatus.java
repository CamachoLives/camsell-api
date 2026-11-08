package com.camsell.catalog.domain.model;

/** Estado de una oferta de compra. */
public enum OfferStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    public boolean isFinal() {
        return this != PENDING;
    }

    /** Resultado negativo: rechazada por el vendedor o retirada por el comprador. */
    public boolean isNegative() {
        return this == REJECTED || this == WITHDRAWN;
    }
}
