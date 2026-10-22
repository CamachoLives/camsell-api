package com.camsell.catalog.domain.port.out;

/**
 * Datos del vendedor que necesita el catalogo. Evita que este contexto dependa
 * del modelo interno de identidad.
 */
public interface SellerLookupPort {

    SellerInfo findSeller(Long userId);

    record SellerInfo(Long id, String fullName, String phone, String email, String role) {
    }
}
