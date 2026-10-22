package com.camsell.parameters.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParameterGroupTest {

    @Test
    @DisplayName("el codigo de un grupo sigue la convencion MAYUSCULAS_CON_GUION_BAJO")
    void valida_el_formato_del_codigo() {
        assertThatThrownBy(() -> group("fuel type", ParameterDataType.OPTION, false).validate())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("MAYUSCULAS");
        group("FUEL_TYPE", ParameterDataType.OPTION, false).validate();
    }

    @Test
    @DisplayName("solo los grupos de opciones admiten varios valores")
    void solo_option_es_multivalor() {
        assertThatThrownBy(() -> group("HORSEPOWER", ParameterDataType.NUMBER, true).validate())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("OPTION");
        group("FEATURE", ParameterDataType.OPTION, true).validate();
    }

    @Test
    @DisplayName("un grupo obligatorio para publicar debe aplicar al catalogo")
    void obligatorio_requiere_ambito_de_catalogo() {
        ParameterGroup invalido = new ParameterGroup(
                null, "NEWS_CATEGORY", "Categoria", null, ParameterDataType.OPTION,
                ParameterScope.NEWS, false, false, true, false, 10, true, List.of());

        assertThatThrownBy(invalido::validate)
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("LISTING");
    }

    @Test
    @DisplayName("el ambito decide si el grupo participa en una publicacion")
    void el_ambito_define_donde_aplica() {
        assertThat(ParameterScope.LISTING.appliesToListings()).isTrue();
        assertThat(ParameterScope.GENERAL.appliesToListings()).isTrue();
        assertThat(ParameterScope.NEWS.appliesToListings()).isFalse();
    }

    @Test
    @DisplayName("sin ambito explicito el grupo aplica al catalogo")
    void ambito_por_defecto() {
        ParameterGroup sinAmbito = new ParameterGroup(
                null, "COLOR", "Color", null, ParameterDataType.OPTION,
                null, false, true, true, true, 10, true, null);

        assertThat(sinAmbito.scope()).isEqualTo(ParameterScope.LISTING);
        assertThat(sinAmbito.options()).isEmpty();
    }

    @Test
    @DisplayName("las opciones inactivas no se ofrecen al publico")
    void filtra_opciones_inactivas() {
        ParameterGroup grupo = group("FUEL_TYPE", ParameterDataType.OPTION, false)
                .withOptions(List.of(
                        new ParameterOption(1L, "FUEL_TYPE", "DIESEL", "Diesel", null, null, 10, true),
                        new ParameterOption(2L, "FUEL_TYPE", "GAS", "Gas", null, null, 20, false)));

        assertThat(grupo.activeOptions()).hasSize(1);
        assertThat(grupo.activeOptions().getFirst().code()).isEqualTo("DIESEL");
    }

    private static ParameterGroup group(String code, ParameterDataType dataType, boolean multiValued) {
        return new ParameterGroup(
                null, code, "Grupo de prueba", null, dataType,
                ParameterScope.LISTING, multiValued, true, false, true, 10, true, List.of());
    }
}
