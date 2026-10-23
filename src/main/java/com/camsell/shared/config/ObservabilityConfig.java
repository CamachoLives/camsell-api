package com.camsell.shared.config;

import io.micrometer.core.aop.CountedAspect;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Instrumentacion de los casos de uso.
 *
 * <p>Las anotaciones {@code @Timed} y {@code @Counted} se resuelven por
 * aspectos, de modo que medir un caso de uso no obliga a inyectar el registro
 * de metricas en el servicio ni a mezclar la medicion con la regla de negocio.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public CountedAspect countedAspect(MeterRegistry registry) {
        return new CountedAspect(registry);
    }

    /**
     * Etiquetas comunes a todas las metricas. Permiten distinguir instancias,
     * versiones y perfiles cuando varias publican al mismo Prometheus.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> commonTags(
            Environment environment,
            ObjectProvider<BuildProperties> buildProperties) {
        BuildProperties build = buildProperties.getIfAvailable();
        String version = build == null ? "dev" : build.getVersion();
        String profiles = String.join(",", environment.getActiveProfiles());

        return registry -> registry.config().commonTags(
                "application", "camsell-api",
                "version", version,
                "profile", profiles.isBlank() ? "default" : profiles);
    }
}
