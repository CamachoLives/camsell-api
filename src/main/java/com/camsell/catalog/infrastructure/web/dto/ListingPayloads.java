package com.camsell.catalog.infrastructure.web.dto;

import com.camsell.catalog.domain.model.ListingStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class ListingPayloads {

    private ListingPayloads() {
    }

    public record SaveListing(
            Long brandId,

            @NotNull(message = "El modelo es obligatorio")
            Long modelId,

            @Size(max = 200, message = "El titulo admite maximo 200 caracteres")
            String title,

            @Min(value = 1900, message = "El ano no es valido")
            @Max(value = 2100, message = "El ano no es valido")
            int year,

            @Min(value = 0, message = "El kilometraje no puede ser negativo")
            @Max(value = 2_000_000, message = "El kilometraje no es valido")
            int mileageKm,

            @NotNull(message = "El precio es obligatorio")
            @DecimalMin(value = "1", message = "El precio debe ser mayor que cero")
            BigDecimal price,

            @Size(max = 3)
            String currency,

            boolean negotiable,

            @NotBlank(message = "La descripcion es obligatoria")
            @Size(max = 6000, message = "La descripcion admite maximo 6000 caracteres")
            String description,

            @Size(max = 60)
            String cityCode,

            boolean publishNow,

            @Valid
            List<Image> images,

            @Valid
            List<Attribute> attributes) {
    }

    public record Image(
            @NotBlank(message = "La URL de la imagen es obligatoria")
            @Size(max = 500)
            String url,

            @Size(max = 200)
            String alt,

            Integer position) {
    }

    /** Valor de un parametro; el grupo determina cual de los campos se usa. */
    public record Attribute(
            @NotBlank(message = "El grupo del parametro es obligatorio")
            @Size(max = 40)
            String groupCode,

            @Size(max = 60)
            String optionCode,

            @Size(max = 500)
            String textValue,

            BigDecimal numberValue,

            Boolean boolValue) {
    }

    public record ChangeStatus(
            @NotNull(message = "El estado es obligatorio")
            ListingStatus status) {
    }
}
