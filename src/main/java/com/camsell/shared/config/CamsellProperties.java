package com.camsell.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

/**
 * Configuracion tipada de la plataforma. Todo valor sensible se inyecta por
 * variable de entorno en despliegue.
 */
@ConfigurationProperties(prefix = "camsell")
public record CamsellProperties(Jwt jwt, Cors cors, Catalog catalog, RateLimit rateLimit) {

    public CamsellProperties {
        rateLimit = rateLimit == null ? RateLimit.defaults() : rateLimit;
    }

    public record Jwt(String secret, String issuer, Duration accessTokenTtl) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    /** Limites parametrizables del catalogo, ajustables sin recompilar. */
    public record Catalog(int maxImagesPerListing, int maxFeaturedListings, int minYear) {
    }

    /**
     * Cuotas de peticiones por cliente. Las credenciales tienen una cuota
     * aparte, mucho mas estrecha, para frenar la fuerza bruta.
     */
    public record RateLimit(
            boolean enabled,
            int credentialAttemptsPerMinute,
            int writesPerMinute) {

        public static RateLimit defaults() {
            return new RateLimit(true, 10, 60);
        }
    }
}
