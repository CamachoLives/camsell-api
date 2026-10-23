package com.camsell.shared.api;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cubeta de tokens por clave, con recarga continua.
 *
 * <p>Se eligio una implementacion propia en lugar de una libreria porque el
 * requisito es acotado y no justifica una dependencia: lo unico que hace falta
 * es frenar la fuerza bruta contra el inicio de sesion y poner un techo a las
 * escrituras por cliente.
 *
 * <p>La recarga es proporcional al tiempo transcurrido, no por ventanas fijas,
 * para que nadie pueda gastar el doble de la cuota a caballo entre dos
 * ventanas.
 */
public class RateLimiter {

    /** Tope de claves vigiladas, para que el mapa no crezca sin limite. */
    private static final int MAX_TRACKED_KEYS = 50_000;

    private final int capacity;
    private final double tokensPerMilli;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int capacity, Duration refillWindow) {
        this.capacity = capacity;
        this.tokensPerMilli = (double) capacity / refillWindow.toMillis();
    }

    /** Consume un token. Devuelve los segundos de espera, o cero si se permite. */
    public long tryConsume(String key) {
        if (buckets.size() > MAX_TRACKED_KEYS) {
            // Ante un flujo de claves distintas se prefiere reiniciar el conteo
            // antes que agotar la memoria del proceso.
            buckets.clear();
        }
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket(capacity));
        return bucket.consume(tokensPerMilli, capacity);
    }

    /** Libera el estado acumulado. Se usa al cerrar sesion con exito y en pruebas. */
    public void reset(String key) {
        buckets.remove(key);
    }

    public void resetAll() {
        buckets.clear();
    }

    private static final class Bucket {

        private double tokens;
        private long lastRefillAt;

        private Bucket(int capacity) {
            this.tokens = capacity;
            this.lastRefillAt = System.currentTimeMillis();
        }

        private synchronized long consume(double tokensPerMilli, int capacity) {
            long now = System.currentTimeMillis();
            tokens = Math.min(capacity, tokens + (now - lastRefillAt) * tokensPerMilli);
            lastRefillAt = now;

            if (tokens >= 1) {
                tokens -= 1;
                return 0;
            }
            double missing = 1 - tokens;
            return Math.max(1, (long) Math.ceil(missing / tokensPerMilli / 1000));
        }
    }
}
