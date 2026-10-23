package com.camsell.catalog.domain.port.in;

import com.camsell.catalog.domain.model.ListingCard;

import java.util.List;

public interface FavoriteUseCase {

    /** Agrega o quita la publicacion de favoritos y devuelve el estado final. */
    boolean toggle(Long listingId);

    List<ListingCard> listMine();

    boolean isFavorite(Long listingId);
}
