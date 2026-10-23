package com.camsell.shared.api;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.time.Duration;

/**
 * Politicas de cache HTTP de la API publica.
 *
 * <p>El esquema de parametros y el arbol de marcas cambian pocas veces al dia y
 * se piden en cada pantalla, asi que se sirven con cache publica. El catalogo y
 * las noticias cambian seguido, pero tolera unos segundos de desfase, lo que
 * absorbe las rafagas de peticiones mientras alguien ajusta filtros.
 */
public final class CachePolicy {

    /** Datos de configuracion: cambian cuando un administrador los edita. */
    public static final CacheControl CONFIGURATION = CacheControl
            .maxAge(Duration.ofMinutes(5))
            .staleWhileRevalidate(Duration.ofMinutes(30))
            .cachePublic();

    /** Resultados de busqueda y feeds: ventana corta para absorber rafagas. */
    public static final CacheControl FEED = CacheControl
            .maxAge(Duration.ofSeconds(30))
            .staleWhileRevalidate(Duration.ofMinutes(2))
            .cachePublic();

    /** Fichas individuales, que incluyen contadores de visitas. */
    public static final CacheControl DETAIL = CacheControl
            .maxAge(Duration.ofSeconds(15))
            .cachePublic();

    /** Respuestas que dependen del usuario autenticado. */
    public static final CacheControl PRIVATE = CacheControl.noStore();

    private CachePolicy() {
    }

    public static <T> ResponseEntity<T> publicResponse(T body, CacheControl cacheControl) {
        return ResponseEntity.ok().cacheControl(cacheControl).body(body);
    }

    /** Respuesta cacheable que ademas se puede revalidar con {@code If-None-Match}. */
    public static <T> ResponseEntity<T> validated(T body, CacheControl cacheControl, String etag) {
        return ResponseEntity.ok().cacheControl(cacheControl).eTag(etag).body(body);
    }
}
