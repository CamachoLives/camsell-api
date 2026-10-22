package com.camsell.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuracion tipada de la plataforma. Todo valor sensible se inyecta por
 * variable de entorno en despliegue.
 */
@ConfigurationProperties(prefix = "camsell")
public record CamsellProperties(Jwt jwt, Cors cors, Catalog catalog) {

    public record Jwt(String secret, String issuer, Duration accessTokenTtl) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    /** Limites parametrizables del catalogo, ajustables sin recompilar. */
    public record Catalog(int maxImagesPerListing, int maxFeaturedListings, int minYear) {
    }
}
