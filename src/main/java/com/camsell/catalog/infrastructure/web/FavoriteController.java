package com.camsell.catalog.infrastructure.web;

import com.camsell.catalog.domain.port.in.FavoriteUseCase;
import com.camsell.catalog.infrastructure.web.dto.ListingResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteUseCase favorites;

    public FavoriteController(FavoriteUseCase favorites) {
        this.favorites = favorites;
    }

    @GetMapping
    public List<ListingResponses.Summary> mine() {
        return favorites.listMine().stream().map(ListingResponses.Summary::from).toList();
    }

    @PostMapping("/{listingId}/toggle")
    public Map<String, Boolean> toggle(@PathVariable Long listingId) {
        return Map.of("favorite", favorites.toggle(listingId));
    }
}
