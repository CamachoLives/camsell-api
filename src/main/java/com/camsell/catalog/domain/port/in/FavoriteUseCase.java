package com.camsell.catalog.domain.port.in;

import com.camsell.catalog.domain.model.Listing;

import java.util.List;

public interface FavoriteUseCase {

    /** Agrega o quita la publicacion de favoritos y devuelve el estado final. */
    boolean toggle(Long listingId);

    List<Listing> listMine();

    boolean isFavorite(Long listingId);
}
