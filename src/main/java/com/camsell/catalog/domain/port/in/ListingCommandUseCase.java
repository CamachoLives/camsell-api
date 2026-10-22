package com.camsell.catalog.domain.port.in;

import com.camsell.catalog.domain.model.Listing;
import com.camsell.catalog.domain.model.ListingStatus;

import java.math.BigDecimal;
import java.util.List;

public interface ListingCommandUseCase {

    Listing create(SaveListingCommand command);

    Listing update(Long id, SaveListingCommand command);

    Listing changeStatus(Long id, ListingStatus status);

    Listing setFeatured(Long id, boolean featured);

    void delete(Long id);

    record SaveListingCommand(
            Long brandId,
            Long modelId,
            String title,
            int year,
            int mileageKm,
            BigDecimal price,
            String currency,
            boolean negotiable,
            String description,
            String cityCode,
            boolean publishNow,
            List<ImageCommand> images,
            List<AttributeCommand> attributes) {
    }

    record ImageCommand(String url, String alt, Integer position) {
    }

    /**
     * Valor de un parametro. Se envia el grupo y, segun su tipo de dato, el
     * codigo de opcion o el valor literal.
     */
    record AttributeCommand(
            String groupCode,
            String optionCode,
            String textValue,
            BigDecimal numberValue,
            Boolean boolValue) {
    }
}
