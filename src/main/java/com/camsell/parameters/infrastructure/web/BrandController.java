package com.camsell.parameters.infrastructure.web;

import com.camsell.parameters.domain.port.in.BrandUseCase;
import com.camsell.parameters.infrastructure.web.dto.BrandResponse;
import com.camsell.shared.api.CachePolicy;
import com.camsell.shared.api.ResourceVersions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/brands")
public class BrandController {

    private final BrandUseCase brands;
    private final ResourceVersions versions;

    public BrandController(BrandUseCase brands, ResourceVersions versions) {
        this.brands = brands;
        this.versions = versions;
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> list(
            @RequestParam(defaultValue = "true") boolean withModels,
            WebRequest request) {
        String etag = versions.etagOf(ResourceVersions.Family.BRANDS);
        if (request.checkNotModified(etag)) {
            return null;
        }
        List<BrandResponse> body = brands.listBrands(false, withModels).stream()
                .map(BrandResponse::from)
                .toList();
        return CachePolicy.validated(body, CachePolicy.CONFIGURATION, etag);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BrandResponse> detail(@PathVariable String slug, WebRequest request) {
        String etag = versions.etagOf(ResourceVersions.Family.BRANDS);
        if (request.checkNotModified(etag)) {
            return null;
        }
        return CachePolicy.validated(
                BrandResponse.from(brands.getBrand(slug)), CachePolicy.CONFIGURATION, etag);
    }

    @GetMapping("/{brandId}/models")
    public ResponseEntity<List<BrandResponse.Model>> models(
            @PathVariable Long brandId, WebRequest request) {
        String etag = versions.etagOf(ResourceVersions.Family.BRANDS);
        if (request.checkNotModified(etag)) {
            return null;
        }
        List<BrandResponse.Model> body = brands.listModels(brandId, false).stream()
                .map(BrandResponse.Model::from)
                .toList();
        return CachePolicy.validated(body, CachePolicy.CONFIGURATION, etag);
    }
}
