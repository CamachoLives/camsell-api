package com.camsell.catalog.infrastructure.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ListingControllerTest {

    @Test
    @DisplayName("convierte parametros GRUPO:OPCION en filtros agrupados")
    void agrupa_filtros_por_grupo() {
        Map<String, List<String>> filters = ListingController.parseAttributeFilters(
                List.of("FUEL_TYPE:DIESEL", "FUEL_TYPE:HYBRID", "TRANSMISSION:AUTOMATIC"));

        assertThat(filters).hasSize(2);
        assertThat(filters.get("FUEL_TYPE")).containsExactly("DIESEL", "HYBRID");
        assertThat(filters.get("TRANSMISSION")).containsExactly("AUTOMATIC");
    }

    @Test
    @DisplayName("ignora entradas mal formadas en lugar de romper la busqueda")
    void ignora_entradas_invalidas() {
        Map<String, List<String>> filters = ListingController.parseAttributeFilters(
                List.of("sin-separador", ":SOLO_OPCION", "SOLO_GRUPO:", "  ", "COLOR:RED"));

        assertThat(filters).containsOnlyKeys("COLOR");
        assertThat(filters.get("COLOR")).containsExactly("RED");
    }

    @Test
    @DisplayName("sin filtros devuelve un mapa vacio")
    void sin_filtros_mapa_vacio() {
        assertThat(ListingController.parseAttributeFilters(null)).isEmpty();
        assertThat(ListingController.parseAttributeFilters(List.of())).isEmpty();
    }
}
