package com.camsell.catalog;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cualquier grupo marcado como filtrable debe poder filtrar, sin importar en
 * que columna guarde su valor segun el tipo de dato.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:camsell-attr;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class ListingAttributeFilterTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("filtra por un grupo de opciones")
    void filtra_por_opcion() throws Exception {
        mockMvc().perform(get("/api/v1/listings").param("attr", "DRIVETRAIN:4WD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("filtra por un grupo booleano, cuyo valor vive en bool_value")
    void filtra_por_booleano() throws Exception {
        mockMvc().perform(get("/api/v1/listings").param("attr", "SINGLE_OWNER:true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(6));
    }

    @Test
    @DisplayName("filtra por un grupo numerico, cuyo valor vive en number_value")
    void filtra_por_numero() throws Exception {
        mockMvc().perform(get("/api/v1/listings").param("attr", "DOORS:4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    @DisplayName("varias opciones del mismo grupo se combinan con OR")
    void combina_opciones_con_or() throws Exception {
        mockMvc()
                .perform(get("/api/v1/listings")
                        .param("attr", "FUEL_TYPE:DIESEL")
                        .param("attr", "FUEL_TYPE:ELECTRIC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    @DisplayName("grupos distintos se combinan con AND")
    void combina_grupos_con_and() throws Exception {
        mockMvc()
                .perform(get("/api/v1/listings")
                        .param("attr", "BODY_TYPE:SUV")
                        .param("attr", "FUEL_TYPE:HYBRID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("un valor que no existe no devuelve resultados, sin fallar")
    void valor_inexistente_no_falla() throws Exception {
        mockMvc().perform(get("/api/v1/listings").param("attr", "FUEL_TYPE:NO_EXISTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
