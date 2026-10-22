package com.camsell.shared.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Los parametros del sistema cambian poco y se leen en cada pantalla, por eso
 * viven en cache y se invalidan explicitamente al administrarlos.
 */
@Configuration
public class CacheConfig {

    public static final String PARAMETER_GROUPS = "parameterGroups";
    public static final String PARAMETER_OPTIONS = "parameterOptions";
    public static final String BRANDS = "brands";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(PARAMETER_GROUPS, PARAMETER_OPTIONS, BRANDS);
    }
}
