package com.camsell.catalog.infrastructure.web;

import com.camsell.catalog.domain.port.in.ListingQueryUseCase;
import com.camsell.shared.api.CachePolicy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Indicadores que alimentan la portada del sitio. */
@RestController
@RequestMapping("/api/v1/stats")
public class CatalogStatsController {

    private final ListingQueryUseCase listings;

    public CatalogStatsController(ListingQueryUseCase listings) {
        this.listings = listings;
    }

    @GetMapping("/catalog")
    public ResponseEntity<ListingQueryUseCase.CatalogStats> catalog() {
        return CachePolicy.publicResponse(listings.stats(), CachePolicy.FEED);
    }
}
