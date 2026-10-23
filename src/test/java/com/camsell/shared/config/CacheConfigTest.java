package com.camsell.shared.config;

import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:camsell-cacheconf;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ParameterQueryUseCase parameters;

    @Autowired
    private MeterRegistry registry;

    private Cache<Object, Object> nativeCache(String name) {
        return ((CaffeineCache) cacheManager.getCache(name)).getNativeCache();
    }

    @Test
    @DisplayName("las caches declaradas existen y son Caffeine")
    void declara_las_caches_esperadas() {
        assertThat(cacheManager.getCacheNames())
                .containsExactlyInAnyOrder(
                        CacheConfig.PARAMETER_GROUPS,
                        CacheConfig.PARAMETER_OPTIONS,
                        CacheConfig.BRANDS);
        assertThat(cacheManager.getCache(CacheConfig.PARAMETER_GROUPS))
                .isInstanceOf(CaffeineCache.class);
    }

    @Test
    @DisplayName("un nombre de cache no declarado no se crea en silencio")
    void no_crea_caches_sobre_la_marcha() {
        assertThat(cacheManager.getCache("caheQueNoExiste")).isNull();
    }

    @Test
    @DisplayName("la segunda lectura del esquema es un acierto de cache")
    void la_segunda_lectura_golpea_la_cache() {
        cacheManager.getCache(CacheConfig.PARAMETER_GROUPS).clear();

        parameters.listGroups(false);
        parameters.listGroups(false);
        parameters.listGroups(false);

        CacheStats stats = nativeCache(CacheConfig.PARAMETER_GROUPS).stats();

        assertThat(stats.hitCount()).as("aciertos").isGreaterThanOrEqualTo(2);
        assertThat(stats.missCount()).as("fallos").isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("las estadisticas de la cache se publican como metricas")
    void publica_metricas_de_cache() {
        parameters.listGroups(false);

        assertThat(registry.find("cache.gets").tag("cache", CacheConfig.PARAMETER_GROUPS).meters())
                .as("metricas de la cache de parametros")
                .isNotEmpty();
    }

    @Test
    @DisplayName("la cache tiene cota de tamano, para no crecer sin limite")
    void la_cache_esta_acotada() {
        assertThat(nativeCache(CacheConfig.BRANDS).policy().eviction())
                .isPresent()
                .get()
                .satisfies(eviction -> assertThat(eviction.getMaximum()).isEqualTo(500));
    }

    @Test
    @DisplayName("la cache vence por tiempo, como red de seguridad")
    void la_cache_vence() {
        assertThat(nativeCache(CacheConfig.BRANDS).policy().expireAfterWrite())
                .isPresent()
                .get()
                .satisfies(expiry -> assertThat(expiry.getExpiresAfter().toMinutes()).isEqualTo(10));
    }
}
