package com.camsell.parameters.infrastructure.web.dto;

import com.camsell.parameters.domain.model.Brand;
import com.camsell.parameters.domain.model.VehicleModel;

import java.util.List;

public record BrandResponse(
        Long id,
        String name,
        String slug,
        String countryOfOrigin,
        String logoUrl,
        boolean active,
        int modelCount,
        List<Model> models) {

    public record Model(
            Long id,
            Long brandId,
            String brandName,
            String name,
            String slug,
            String bodyTypeCode,
            boolean active) {

        public static Model from(VehicleModel model) {
            return new Model(
                    model.id(),
                    model.brandId(),
                    model.brandName(),
                    model.name(),
                    model.slug(),
                    model.bodyTypeCode(),
                    model.active());
        }
    }

    public static BrandResponse from(Brand brand) {
        return new BrandResponse(
                brand.id(),
                brand.name(),
                brand.slug(),
                brand.countryOfOrigin(),
                brand.logoUrl(),
                brand.active(),
                brand.models().size(),
                brand.models().stream().map(Model::from).toList());
    }
}
