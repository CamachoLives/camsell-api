package com.camsell.parameters.infrastructure.web;

import com.camsell.parameters.domain.port.in.BrandUseCase;
import com.camsell.parameters.infrastructure.web.dto.BrandPayloads;
import com.camsell.parameters.infrastructure.web.dto.BrandResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/brands")
@PreAuthorize("hasRole('ADMIN')")
public class BrandAdminController {

    private final BrandUseCase brands;

    public BrandAdminController(BrandUseCase brands) {
        this.brands = brands;
    }

    @GetMapping
    public List<BrandResponse> list() {
        return brands.listBrands(true, true).stream().map(BrandResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse create(@Valid @RequestBody BrandPayloads.BrandPayload payload) {
        return BrandResponse.from(brands.createBrand(new BrandUseCase.SaveBrandCommand(
                payload.name(), payload.countryOfOrigin(), payload.logoUrl(), payload.activeOrDefault())));
    }

    @PutMapping("/{id}")
    public BrandResponse update(@PathVariable Long id, @Valid @RequestBody BrandPayloads.BrandPayload payload) {
        return BrandResponse.from(brands.updateBrand(id, new BrandUseCase.SaveBrandCommand(
                payload.name(), payload.countryOfOrigin(), payload.logoUrl(), payload.activeOrDefault())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        brands.deleteBrand(id);
    }

    @PostMapping("/{brandId}/models")
    @ResponseStatus(HttpStatus.CREATED)
    public BrandResponse.Model createModel(
            @PathVariable Long brandId,
            @Valid @RequestBody BrandPayloads.ModelPayload payload) {
        return BrandResponse.Model.from(brands.createModel(brandId, new BrandUseCase.SaveModelCommand(
                payload.name(), payload.bodyTypeCode(), payload.activeOrDefault())));
    }

    @PutMapping("/models/{modelId}")
    public BrandResponse.Model updateModel(
            @PathVariable Long modelId,
            @Valid @RequestBody BrandPayloads.ModelPayload payload) {
        return BrandResponse.Model.from(brands.updateModel(modelId, new BrandUseCase.SaveModelCommand(
                payload.name(), payload.bodyTypeCode(), payload.activeOrDefault())));
    }

    @DeleteMapping("/models/{modelId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModel(@PathVariable Long modelId) {
        brands.deleteModel(modelId);
    }
}
