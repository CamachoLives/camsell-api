package com.camsell.catalog.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyAndOfferTest {

    @Test
    @DisplayName("el precio siempre es positivo y con dos decimales")
    void normaliza_el_precio() {
        Money money = new Money(new BigDecimal("89900000.555"), "cop");

        assertThat(money.amount()).isEqualByComparingTo("89900000.56");
        assertThat(money.currency()).isEqualTo("COP");
    }

    @Test
    @DisplayName("un precio en cero o negativo no es valido")
    void rechaza_precios_no_positivos() {
        assertThatThrownBy(() -> new Money(BigDecimal.ZERO, "COP"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new Money(new BigDecimal("-10"), "COP"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> new Money(null, "COP"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("sin moneda explicita se usa la moneda por defecto")
    void aplica_moneda_por_defecto() {
        assertThat(Money.of(new BigDecimal("1000")).currency()).isEqualTo(Money.DEFAULT_CURRENCY);
        assertThat(new Money(new BigDecimal("1000"), "  ").currency()).isEqualTo(Money.DEFAULT_CURRENCY);
    }

    @Test
    @DisplayName("el porcentaje respecto al precio publicado alimenta la regla de oferta minima")
    void calcula_el_porcentaje_frente_al_precio() {
        Money precio = new Money(new BigDecimal("100000000"), "COP");
        Money oferta = new Money(new BigDecimal("35000000"), "COP");

        assertThat(oferta.percentageOf(precio)).isEqualByComparingTo("35.00");
        assertThat(oferta.isLowerThan(precio)).isTrue();
    }

    @Test
    @DisplayName("una oferta resuelta no cambia de estado otra vez")
    void una_oferta_resuelta_es_final() {
        Offer pendiente = offer(OfferStatus.PENDING);
        Offer aceptada = pendiente.transitionTo(OfferStatus.ACCEPTED, Instant.now());

        assertThat(aceptada.status()).isEqualTo(OfferStatus.ACCEPTED);
        assertThat(aceptada.respondedAt()).isNotNull();
        assertThatThrownBy(() -> aceptada.transitionTo(OfferStatus.REJECTED, Instant.now()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya fue resuelta");
    }

    private static Offer offer(OfferStatus status) {
        return new Offer(
                1L, 10L, "CAMS-ABC123", "Toyota Corolla 2022", 7L, 9L, "Carlos Ruiz", "+57 300 000 0000",
                new Money(new BigDecimal("80000000"), "COP"), "Me interesa", status, Instant.now(), null);
    }
}
