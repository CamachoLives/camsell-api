package com.camsell.parameters.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Payloads de administracion de marcas y modelos. */
public final class BrandPayloads {

    private BrandPayloads() {
    }

    public record BrandPayload(
            @NotBlank(message = "El nombre de la marca es obligatorio")
            @Size(max = 80, message = "El nombre admite maximo 80 caracteres")
            String name,

            @Size(max = 80)
            String countryOfOrigin,

            @Size(max = 400)
            String logoUrl,

            Boolean active) {

        public boolean activeOrDefault() {
            return active == null || active;
        }
    }

    public record ModelPayload(
            @NotBlank(message = "El nombre del modelo es obligatorio")
            @Size(max = 80, message = "El nombre admite maximo 80 caracteres")
            String name,

            @Size(max = 60)
            String bodyTypeCode,

            Boolean active) {

        public boolean activeOrDefault() {
            return active == null || active;
        }
    }
}
