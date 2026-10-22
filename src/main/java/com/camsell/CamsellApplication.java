package com.camsell;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Punto de entrada de Camsell API.
 *
 * <p>La aplicacion es un monolito modular: cada contexto acotado
 * ({@code identity}, {@code parameters}, {@code catalog}, {@code news}) se
 * organiza en arquitectura hexagonal con las capas {@code domain},
 * {@code application} e {@code infrastructure}, y solo se comunica con los
 * demas a traves de puertos publicos.
 */
@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class CamsellApplication {

    public static void main(String[] args) {
        SpringApplication.run(CamsellApplication.class, args);
    }
}
