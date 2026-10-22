package com.camsell.parameters.infrastructure.persistence;

import com.camsell.parameters.domain.model.Brand;
import com.camsell.parameters.domain.model.VehicleModel;
import com.camsell.parameters.domain.port.out.BrandRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Adaptador JPA del arbol marca / modelo. */
@Repository
public class BrandPersistenceAdapter implements BrandRepositoryPort {

    private final BrandJpaRepository brands;
    private final VehicleModelJpaRepository models;

    public BrandPersistenceAdapter(BrandJpaRepository brands, VehicleModelJpaRepository models) {
        this.brands = brands;
        this.models = models;
    }

    @Override
    public List<Brand> findAllBrands(boolean includeInactive, boolean withModels) {
        List<BrandEntity> entities = includeInactive
                ? brands.findAllByOrderByNameAsc()
                : brands.findAllByActiveTrueOrderByNameAsc();
        if (!withModels || entities.isEmpty()) {
            return entities.stream().map(entity -> toDomain(entity, List.of())).toList();
        }

        Map<Long, String> brandNames = entities.stream()
                .collect(Collectors.toMap(BrandEntity::getId, BrandEntity::getName));
        List<Long> ids = List.copyOf(brandNames.keySet());
        List<VehicleModelEntity> modelEntities = includeInactive
                ? models.findByBrandIdInOrderByNameAsc(ids)
                : models.findByBrandIdInAndActiveTrueOrderByNameAsc(ids);
        Map<Long, List<VehicleModel>> byBrand = modelEntities.stream()
                .map(entity -> toDomain(entity, brandNames.get(entity.getBrandId())))
                .collect(Collectors.groupingBy(VehicleModel::brandId));

        return entities.stream()
                .map(entity -> toDomain(entity, byBrand.getOrDefault(entity.getId(), List.of())))
                .toList();
    }

    @Override
    public Optional<Brand> findBrandBySlug(String slug) {
        return brands.findBySlug(slug).map(this::withModels);
    }

    @Override
    public Optional<Brand> findBrandById(Long id) {
        return brands.findById(id).map(this::withModels);
    }

    @Override
    public boolean brandExistsByName(String name) {
        return brands.existsByNameIgnoreCase(name.trim());
    }

    @Override
    public Brand saveBrand(Brand brand) {
        BrandEntity entity = brand.id() == null
                ? new BrandEntity()
                : brands.findById(brand.id()).orElseGet(BrandEntity::new);
        entity.setName(brand.name());
        entity.setSlug(brand.slug());
        entity.setCountryOfOrigin(brand.countryOfOrigin());
        entity.setLogoUrl(brand.logoUrl());
        entity.setActive(brand.active());
        return toDomain(brands.save(entity), brand.models());
    }

    @Override
    public void deleteBrand(Long id) {
        models.deleteByBrandId(id);
        brands.deleteById(id);
    }

    @Override
    public List<VehicleModel> findModelsByBrandId(Long brandId, boolean includeInactive) {
        String brandName = brands.findById(brandId).map(BrandEntity::getName).orElse(null);
        List<VehicleModelEntity> entities = includeInactive
                ? models.findByBrandIdOrderByNameAsc(brandId)
                : models.findByBrandIdAndActiveTrueOrderByNameAsc(brandId);
        return entities.stream().map(entity -> toDomain(entity, brandName)).toList();
    }

    @Override
    public Optional<VehicleModel> findModelById(Long id) {
        return models.findById(id).map(entity -> toDomain(
                entity,
                brands.findById(entity.getBrandId()).map(BrandEntity::getName).orElse(null)));
    }

    @Override
    public boolean modelExists(Long brandId, String name) {
        return models.existsByBrandIdAndNameIgnoreCase(brandId, name.trim());
    }

    @Override
    public VehicleModel saveModel(VehicleModel model) {
        VehicleModelEntity entity = model.id() == null
                ? new VehicleModelEntity()
                : models.findById(model.id()).orElseGet(VehicleModelEntity::new);
        entity.setBrandId(model.brandId());
        entity.setName(model.name());
        entity.setSlug(model.slug());
        entity.setBodyTypeCode(model.bodyTypeCode());
        entity.setActive(model.active());
        return toDomain(models.save(entity), model.brandName());
    }

    @Override
    public void deleteModel(Long id) {
        models.deleteById(id);
    }

    private Brand withModels(BrandEntity entity) {
        List<VehicleModel> brandModels = models.findByBrandIdOrderByNameAsc(entity.getId()).stream()
                .map(model -> toDomain(model, entity.getName()))
                .toList();
        return toDomain(entity, brandModels);
    }

    private Brand toDomain(BrandEntity entity, List<VehicleModel> brandModels) {
        return new Brand(
                entity.getId(),
                entity.getName(),
                entity.getSlug(),
                entity.getCountryOfOrigin(),
                entity.getLogoUrl(),
                entity.isActive(),
                brandModels);
    }

    private VehicleModel toDomain(VehicleModelEntity entity, String brandName) {
        return new VehicleModel(
                entity.getId(),
                entity.getBrandId(),
                brandName,
                entity.getName(),
                entity.getSlug(),
                entity.getBodyTypeCode(),
                entity.isActive());
    }
}
