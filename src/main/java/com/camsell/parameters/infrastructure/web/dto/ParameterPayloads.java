package com.camsell.parameters.infrastructure.web.dto;

import com.camsell.parameters.domain.model.ParameterDataType;
import com.camsell.parameters.domain.model.ParameterScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Payloads de administracion del catalogo de parametros. */
public final class ParameterPayloads {

    private ParameterPayloads() {
    }

    public record GroupPayload(
            @NotBlank(message = "El codigo es obligatorio")
            @Size(max = 40, message = "El codigo admite maximo 40 caracteres")
            String code,

            @NotBlank(message = "El nombre es obligatorio")
            @Size(max = 120, message = "El nombre admite maximo 120 caracteres")
            String name,

            @Size(max = 400, message = "La descripcion admite maximo 400 caracteres")
            String description,

            @NotNull(message = "El tipo de dato es obligatorio")
            ParameterDataType dataType,

            ParameterScope scope,

            boolean multiValued,
            boolean filterable,
            boolean requiredForListing,
            boolean showInSpecs,
            Integer sortOrder,
            Boolean active) {

        public boolean activeOrDefault() {
            return active == null || active;
        }

        public ParameterScope scopeOrDefault() {
            return scope == null ? ParameterScope.LISTING : scope;
        }
    }

    public record OptionPayload(
            @NotBlank(message = "El codigo es obligatorio")
            @Size(max = 60, message = "El codigo admite maximo 60 caracteres")
            String code,

            @NotBlank(message = "La etiqueta es obligatoria")
            @Size(max = 120, message = "La etiqueta admite maximo 120 caracteres")
            String label,

            @Size(max = 60)
            String iconName,

            @Pattern(regexp = "^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$|^$",
                    message = "El color debe ser hexadecimal, por ejemplo #1f2937")
            String colorHex,

            Integer sortOrder,
            Boolean active) {

        public boolean activeOrDefault() {
            return active == null || active;
        }
    }
}
