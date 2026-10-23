package com.camsell.shared.api;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Version de cada familia de recursos de configuracion, usada como ETag.
 *
 * <p>La alternativa habitual, {@code ShallowEtagHeaderFilter}, calcula el hash
 * del cuerpo ya serializado: obliga a almacenar la respuesta en memoria y, al
 * fijar {@code Content-Length}, impide que el contenedor la comprima. Con un
 * contador por familia el ETag se conoce antes de armar la respuesta, de modo
 * que un 304 se resuelve sin tocar la base de datos ni serializar nada, y la
 * compresion sigue activa para las respuestas que si se envian.
 *
 * <p>El token incluye el instante de arranque para que un despliegue invalide
 * las copias anteriores, aunque el contador vuelva a empezar en cero.
 */
@Component
public class ResourceVersions {

    /** Familias de configuracion que cambian solo por accion de un administrador. */
    public enum Family {
        PARAMETERS,
        BRANDS
    }

    private final long bootId = System.currentTimeMillis();
    private final Map<Family, AtomicLong> revisions = new ConcurrentHashMap<>();

    /**
     * ETag de la familia, listo para comparar contra {@code If-None-Match}.
     *
     * <p>Es deliberadamente debil ({@code W/}). Un ETag fuerte identifica una
     * secuencia de bytes exacta, asi que el contenedor se niega a comprimir la
     * respuesta para no invalidarlo. Este token identifica la version logica
     * del recurso, equivalente comprimida o sin comprimir, de modo que gzip y
     * la revalidacion conviven.
     */
    public String etagOf(Family family) {
        return "W/\"%s-%x-%d\"".formatted(
                family.name().toLowerCase(), bootId, revisionOf(family).get());
    }

    /** Invalida las copias cacheadas de la familia. Se llama tras cada escritura. */
    public void bump(Family family) {
        revisionOf(family).incrementAndGet();
    }

    private AtomicLong revisionOf(Family family) {
        return revisions.computeIfAbsent(family, key -> new AtomicLong());
    }
}
