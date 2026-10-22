package com.camsell.parameters.domain.port.in;

import com.camsell.parameters.domain.model.Brand;
import com.camsell.parameters.domain.model.VehicleModel;

import java.util.List;

/** Consulta y administracion del arbol marca / modelo. */
public interface BrandUseCase {

    List<Brand> listBrands(boolean includeInactive, boolean withModels);

    Brand getBrand(String slug);

    List<VehicleModel> listModels(Long brandId, boolean includeInactive);

    VehicleModel getModel(Long modelId);

    Brand createBrand(SaveBrandCommand command);

    Brand updateBrand(Long id, SaveBrandCommand command);

    void deleteBrand(Long id);

    VehicleModel createModel(Long brandId, SaveModelCommand command);

    VehicleModel updateModel(Long modelId, SaveModelCommand command);

    void deleteModel(Long modelId);

    record SaveBrandCommand(String name, String countryOfOrigin, String logoUrl, boolean active) {
    }

    record SaveModelCommand(String name, String bodyTypeCode, boolean active) {
    }
}
