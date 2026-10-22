package com.camsell.parameters.domain.port.out;

import com.camsell.parameters.domain.model.Brand;
import com.camsell.parameters.domain.model.VehicleModel;

import java.util.List;
import java.util.Optional;

/** Puerto de salida del arbol marca / modelo. */
public interface BrandRepositoryPort {

    List<Brand> findAllBrands(boolean includeInactive, boolean withModels);

    Optional<Brand> findBrandBySlug(String slug);

    Optional<Brand> findBrandById(Long id);

    boolean brandExistsByName(String name);

    Brand saveBrand(Brand brand);

    void deleteBrand(Long id);

    List<VehicleModel> findModelsByBrandId(Long brandId, boolean includeInactive);

    Optional<VehicleModel> findModelById(Long id);

    boolean modelExists(Long brandId, String name);

    VehicleModel saveModel(VehicleModel model);

    void deleteModel(Long id);
}
