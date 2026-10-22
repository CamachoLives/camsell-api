package com.camsell.parameters.infrastructure.web;

import com.camsell.parameters.domain.port.in.BrandUseCase;
import com.camsell.parameters.infrastructure.web.dto.BrandResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandUseCase brands;

    public BrandController(BrandUseCase brands) {
        this.brands = brands;
    }

    @GetMapping
    public List<BrandResponse> list(@RequestParam(defaultValue = "true") boolean withModels) {
        return brands.listBrands(false, withModels).stream().map(BrandResponse::from).toList();
    }

    @GetMapping("/{slug}")
    public BrandResponse detail(@PathVariable String slug) {
        return BrandResponse.from(brands.getBrand(slug));
    }

    @GetMapping("/{brandId}/models")
    public List<BrandResponse.Model> models(@PathVariable Long brandId) {
        return brands.listModels(brandId, false).stream().map(BrandResponse.Model::from).toList();
    }
}
