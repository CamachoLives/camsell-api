package com.camsell.parameters.application;

import com.camsell.parameters.domain.model.Brand;
import com.camsell.parameters.domain.model.ParameterGroups;
import com.camsell.parameters.domain.model.VehicleModel;
import com.camsell.parameters.domain.port.in.BrandUseCase;
import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.parameters.domain.port.out.BrandRepositoryPort;
import com.camsell.shared.config.CacheConfig;
import com.camsell.shared.domain.exception.DuplicatedResourceException;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import com.camsell.shared.domain.util.Slugs;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BrandService implements BrandUseCase {

    private final BrandRepositoryPort repository;
    private final ParameterQueryUseCase parameters;

    public BrandService(BrandRepositoryPort repository, ParameterQueryUseCase parameters) {
        this.repository = repository;
        this.parameters = parameters;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.BRANDS, key = "#includeInactive + ':' + #withModels")
    public List<Brand> listBrands(boolean includeInactive, boolean withModels) {
        return repository.findAllBrands(includeInactive, withModels);
    }

    @Override
    @Transactional(readOnly = true)
    public Brand getBrand(String slug) {
        return repository.findBrandBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Marca", slug));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleModel> listModels(Long brandId, boolean includeInactive) {
        requireBrand(brandId);
        return repository.findModelsByBrandId(brandId, includeInactive);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleModel getModel(Long modelId) {
        return repository.findModelById(modelId)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo", modelId));
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.BRANDS, allEntries = true)
    public Brand createBrand(SaveBrandCommand command) {
        if (repository.brandExistsByName(command.name())) {
            throw new DuplicatedResourceException("Ya existe la marca " + command.name());
        }
        return repository.saveBrand(new Brand(
                null,
                command.name().trim(),
                Slugs.of(command.name()),
                command.countryOfOrigin(),
                command.logoUrl(),
                command.active(),
                List.of()));
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.BRANDS, allEntries = true)
    public Brand updateBrand(Long id, SaveBrandCommand command) {
        Brand current = requireBrand(id);
        return repository.saveBrand(new Brand(
                current.id(),
                command.name().trim(),
                Slugs.of(command.name()),
                command.countryOfOrigin(),
                command.logoUrl(),
                command.active(),
                current.models()));
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.BRANDS, allEntries = true)
    public void deleteBrand(Long id) {
        requireBrand(id);
        repository.deleteBrand(id);
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.BRANDS, allEntries = true)
    public VehicleModel createModel(Long brandId, SaveModelCommand command) {
        Brand brand = requireBrand(brandId);
        if (repository.modelExists(brandId, command.name())) {
            throw new DuplicatedResourceException(
                    "La marca %s ya tiene el modelo %s".formatted(brand.name(), command.name()));
        }
        validateBodyType(command.bodyTypeCode());
        return repository.saveModel(new VehicleModel(
                null,
                brandId,
                brand.name(),
                command.name().trim(),
                Slugs.of(brand.name(), command.name()),
                command.bodyTypeCode(),
                command.active()));
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.BRANDS, allEntries = true)
    public VehicleModel updateModel(Long modelId, SaveModelCommand command) {
        VehicleModel current = getModel(modelId);
        Brand brand = requireBrand(current.brandId());
        validateBodyType(command.bodyTypeCode());
        return repository.saveModel(new VehicleModel(
                current.id(),
                current.brandId(),
                brand.name(),
                command.name().trim(),
                Slugs.of(brand.name(), command.name()),
                command.bodyTypeCode(),
                command.active()));
    }

    @Override
    @CacheEvict(cacheNames = CacheConfig.BRANDS, allEntries = true)
    public void deleteModel(Long modelId) {
        getModel(modelId);
        repository.deleteModel(modelId);
    }

    private Brand requireBrand(Long id) {
        return repository.findBrandById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Marca", id));
    }

    private void validateBodyType(String bodyTypeCode) {
        if (bodyTypeCode != null && !bodyTypeCode.isBlank()) {
            parameters.requireValidOption(ParameterGroups.BODY_TYPE, bodyTypeCode);
        }
    }
}
