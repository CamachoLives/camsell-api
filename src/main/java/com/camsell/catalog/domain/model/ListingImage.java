package com.camsell.catalog.domain.model;

public record ListingImage(Long id, String url, String alt, int position) {

    public static ListingImage of(String url, String alt, int position) {
        return new ListingImage(null, url, alt, position);
    }
}
