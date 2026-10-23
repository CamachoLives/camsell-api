package com.camsell.shared.config;

import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad del puerto de gestion.
 *
 * <p>Los endpoints de actuator viven en un puerto aparte que no se publica
 * hacia afuera: las metricas describen el comportamiento interno del sistema y
 * no deberian ser legibles desde internet. Dentro de la red del despliegue, en
 * cambio, el orquestador necesita consultar las sondas de salud y Prometheus
 * recolectar sin credenciales.
 *
 * <p>Esta cadena se declara aparte y con prioridad porque, al existir una
 * {@code SecurityFilterChain} propia, Spring Boot desactiva la configuracion de
 * seguridad que normalmente aplicaria a actuator.
 */
@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ManagementSecurityConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain managementSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }
}
