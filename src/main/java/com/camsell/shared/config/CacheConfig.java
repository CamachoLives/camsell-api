package com.camsell.shared.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Cache de los datos de configuracion.
 *
 * <p>Los parametros y las marcas se leen en cada pantalla y cambian pocas veces
 * al dia, asi que tenerlos en memoria evita ir a la base de datos en cada
 * peticion. La invalidacion explicita al administrarlos es el mecanismo
 * principal; el vencimiento por tiempo es la red de seguridad para el caso en
 * que alguien modifique los datos por fuera de la aplicacion.
 *
 * <p>Se usa Caffeine y no un mapa concurrente porque un mapa sin vencimiento ni
 * cota de tamano crece hasta donde llegue la memoria y no reporta nada: sin
 * estadisticas no hay forma de saber si la cache sirve de algo.
 */
@Configuration
public class CacheConfig {

    public static final String PARAMETER_GROUPS = "parameterGroups";
    public static final String PARAMETER_OPTIONS = "parameterOptions";
    public static final String BRANDS = "brands";

    /** Los datos de configuracion toleran varios minutos de desfase. */
    private static final Duration CONFIGURATION_TTL = Duration.ofMinutes(10);

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        // Sin esto el gestor crea caches sobre la marcha con la configuracion
        // por defecto, y un nombre mal escrito pasaria desapercibido.
        manager.setCacheNames(java.util.List.of(PARAMETER_GROUPS, PARAMETER_OPTIONS, BRANDS));
        manager.setCaffeine(configurationCache());
        return manager;
    }

    private Caffeine<Object, Object> configurationCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(CONFIGURATION_TTL)
                // El esquema completo mas sus variantes por grupo: unas pocas
                // decenas de entradas bastan y el tope evita sorpresas.
                .maximumSize(500)
                // Necesario para que las metricas de aciertos y fallos existan.
                .recordStats();
    }
}
