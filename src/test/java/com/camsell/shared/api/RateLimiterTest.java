package com.camsell.shared.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    @Test
    @DisplayName("permite consumir hasta la capacidad y luego frena")
    void frena_al_agotar_la_cuota() {
        RateLimiter limiter = new RateLimiter(3, Duration.ofMinutes(1));

        assertThat(limiter.tryConsume("cliente")).isZero();
        assertThat(limiter.tryConsume("cliente")).isZero();
        assertThat(limiter.tryConsume("cliente")).isZero();
        assertThat(limiter.tryConsume("cliente")).isPositive();
    }

    @Test
    @DisplayName("al frenar informa cuantos segundos hay que esperar")
    void informa_la_espera() {
        RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1));
        limiter.tryConsume("cliente");

        long wait = limiter.tryConsume("cliente");

        assertThat(wait).isBetween(1L, 60L);
    }

    @Test
    @DisplayName("cada cliente tiene su propia cubeta")
    void las_cubetas_son_por_cliente() {
        RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1));

        assertThat(limiter.tryConsume("uno")).isZero();
        assertThat(limiter.tryConsume("uno")).isPositive();
        assertThat(limiter.tryConsume("dos")).isZero();
    }

    @Test
    @DisplayName("la cuota se recupera con el tiempo, de forma proporcional")
    void la_cuota_se_recarga() throws InterruptedException {
        // Ventana corta para no alargar la prueba: 4 tokens cada 400 ms.
        RateLimiter limiter = new RateLimiter(4, Duration.ofMillis(400));
        for (int attempt = 0; attempt < 4; attempt++) {
            assertThat(limiter.tryConsume("cliente")).isZero();
        }
        assertThat(limiter.tryConsume("cliente")).isPositive();

        Thread.sleep(250);

        assertThat(limiter.tryConsume("cliente"))
                .as("tras recargar parcialmente debe volver a permitir")
                .isZero();
    }

    @Test
    @DisplayName("reiniciar una clave devuelve la cuota completa")
    void reiniciar_libera_la_cuota() {
        RateLimiter limiter = new RateLimiter(1, Duration.ofMinutes(1));
        limiter.tryConsume("cliente");
        assertThat(limiter.tryConsume("cliente")).isPositive();

        limiter.reset("cliente");

        assertThat(limiter.tryConsume("cliente")).isZero();
    }
}
