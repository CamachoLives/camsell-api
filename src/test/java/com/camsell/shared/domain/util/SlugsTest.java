package com.camsell.shared.domain.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugsTest {

    @Test
    @DisplayName("quita acentos y normaliza separadores")
    void normaliza_texto_a_slug() {
        assertThat(Slugs.of("Citroën C3 Aircross")).isEqualTo("citroen-c3-aircross");
        assertThat(Slugs.of("Mazda", "CX-5", "2023")).isEqualTo("mazda-cx-5-2023");
        assertThat(Slugs.of("  Ibagué  ")).isEqualTo("ibague");
    }

    @Test
    @DisplayName("colapsa separadores repetidos y recorta los extremos")
    void colapsa_separadores() {
        assertThat(Slugs.of("Toyota   ///   Hilux")).isEqualTo("toyota-hilux");
        assertThat(Slugs.of("--BMW Serie 3--")).isEqualTo("bmw-serie-3");
    }

    @Test
    @DisplayName("el slug es estable para la misma entrada")
    void es_estable() {
        assertThat(Slugs.of("Renault Duster 2021")).isEqualTo(Slugs.of("Renault Duster 2021"));
    }
}
