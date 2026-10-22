package com.camsell.parameters.domain.model;

import java.util.List;

/** Marca de vehiculo administrable. */
public record Brand(
        Long id,
        String name,
        String slug,
        String countryOfOrigin,
        String logoUrl,
        boolean active,
        List<VehicleModel> models) {

    public Brand {
        models = models == null ? List.of() : List.copyOf(models);
    }

    public Brand withModels(List<VehicleModel> newModels) {
        return new Brand(id, name, slug, countryOfOrigin, logoUrl, active, newModels);
    }
}
