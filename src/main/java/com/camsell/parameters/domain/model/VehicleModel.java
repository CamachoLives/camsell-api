package com.camsell.parameters.domain.model;

/** Modelo perteneciente a una marca (por ejemplo Corolla de Toyota). */
public record VehicleModel(
        Long id,
        Long brandId,
        String brandName,
        String name,
        String slug,
        String bodyTypeCode,
        boolean active) {
}
