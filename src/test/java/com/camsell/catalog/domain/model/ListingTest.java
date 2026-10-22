package com.camsell.catalog.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ListingTest {

    private static final int MAX_IMAGES = 12;
    private static final int MIN_YEAR = 1950;

    @Test
    @DisplayName("una publicacion con datos completos pasa la validacion de publicacion")
    void publica_cuando_los_datos_estan_completos() {
        Listing listing = listing(List.of(image(0)), descripcionValida(), 2022);

        listing.validateForPublication(MAX_IMAGES, MIN_YEAR);
    }

    @Test
    @DisplayName("no se puede publicar sin imagenes")
    void rechaza_publicacion_sin_imagenes() {
        Listing listing = listing(List.of(), descripcionValida(), 2022);

        assertThatThrownBy(() -> listing.validateForPublication(MAX_IMAGES, MIN_YEAR))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("al menos una imagen");
    }

    @Test
    @DisplayName("el limite de imagenes es el configurado por la plataforma")
    void rechaza_mas_imagenes_que_el_limite_configurado() {
        List<ListingImage> images = List.of(image(0), image(1), image(2));
        Listing listing = listing(images, descripcionValida(), 2022);

        assertThatThrownBy(() -> listing.validateForPublication(2, MIN_YEAR))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Maximo 2 imagenes");
    }

    @Test
    @DisplayName("el ano debe estar entre el minimo configurado y el proximo modelo")
    void rechaza_anos_fuera_de_rango() {
        Listing antigua = listing(List.of(image(0)), descripcionValida(), 1940);
        Listing futura = listing(List.of(image(0)), descripcionValida(), Year.now().getValue() + 5);

        assertThatThrownBy(() -> antigua.validateForPublication(MAX_IMAGES, MIN_YEAR))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> futura.validateForPublication(MAX_IMAGES, MIN_YEAR))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("una descripcion muy corta no sale al aire")
    void rechaza_descripcion_corta() {
        Listing listing = listing(List.of(image(0)), "Buen carro", 2022);

        assertThatThrownBy(() -> listing.validateForPublication(MAX_IMAGES, MIN_YEAR))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("40 caracteres");
    }

    @Test
    @DisplayName("al publicar por primera vez se fija la fecha de publicacion")
    void fija_la_fecha_al_publicar() {
        Instant ahora = Instant.parse("2026-03-01T10:15:30Z");
        Listing publicada = listing(List.of(image(0)), descripcionValida(), 2022)
                .withStatus(ListingStatus.PUBLISHED, ahora);

        assertThat(publicada.status()).isEqualTo(ListingStatus.PUBLISHED);
        assertThat(publicada.publishedAt()).isEqualTo(ahora);
    }

    @Test
    @DisplayName("una publicacion vendida solo puede archivarse")
    void una_publicacion_vendida_no_vuelve_atras() {
        Listing vendida = listing(List.of(image(0)), descripcionValida(), 2022)
                .withStatus(ListingStatus.SOLD, Instant.now());

        assertThatThrownBy(() -> vendida.withStatus(ListingStatus.PUBLISHED, Instant.now()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("vendida");
        assertThat(vendida.withStatus(ListingStatus.ARCHIVED, Instant.now()).status())
                .isEqualTo(ListingStatus.ARCHIVED);
    }

    @Test
    @DisplayName("la imagen de portada es la de menor posicion")
    void la_portada_es_la_primera_posicion() {
        Listing listing = listing(List.of(image(3), image(1), image(2)), descripcionValida(), 2022);

        assertThat(listing.coverImage().position()).isEqualTo(1);
    }

    @Test
    @DisplayName("solo las publicaciones visibles reciben ofertas")
    void solo_publicadas_aceptan_ofertas() {
        assertThat(ListingStatus.PUBLISHED.acceptsOffers()).isTrue();
        assertThat(ListingStatus.RESERVED.acceptsOffers()).isFalse();
        assertThat(ListingStatus.PUBLISHED.isPubliclyVisible()).isTrue();
        assertThat(ListingStatus.RESERVED.isPubliclyVisible()).isTrue();
        assertThat(ListingStatus.DRAFT.isPubliclyVisible()).isFalse();
    }

    private static String descripcionValida() {
        return "Vehiculo en excelente estado, mantenimientos al dia y unico propietario desde nuevo.";
    }

    private static ListingImage image(int position) {
        return ListingImage.of("https://camsell.dev/img/" + position + ".jpg", "foto", position);
    }

    private static Listing listing(List<ListingImage> images, String description, int year) {
        return new Listing(
                1L, "CAMS-ABC123", "toyota-corolla-2022", 7L, "Laura Gomez",
                1L, "Toyota", 2L, "Corolla", "Toyota Corolla 2022",
                year, 35_000, new Money(new BigDecimal("89900000"), "COP"), true,
                description, "BOGOTA", ListingStatus.DRAFT, false, 0L, null, null,
                images, List.of());
    }
}
