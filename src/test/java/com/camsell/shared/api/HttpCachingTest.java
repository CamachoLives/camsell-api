package com.camsell.shared.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * El ETag se deriva de una version por familia de recursos y no del hash del
 * cuerpo, asi que un 304 se resuelve sin serializar la respuesta y las
 * respuestas que si se envian siguen pudiendo comprimirse.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:camsell-cache;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class HttpCachingTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ResourceVersions versions;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("el esquema de parametros se sirve con cache publica larga y ETag")
    void parametros_con_cache_de_configuracion() throws Exception {
        mockMvc().perform(get("/api/v1/parameters/groups"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("max-age=300")))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("public")))
                .andExpect(header().exists(HttpHeaders.ETAG));
    }

    @Test
    @DisplayName("la busqueda del catalogo usa una ventana corta de cache")
    void catalogo_con_cache_corta() throws Exception {
        mockMvc().perform(get("/api/v1/listings"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("max-age=30")));
    }

    @Test
    @DisplayName("la ficha del vehiculo no se cachea porque varia por usuario")
    void ficha_no_se_cachea() throws Exception {
        mockMvc().perform(get("/api/v1/listings/toyota-corolla-cross-2024"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")));
    }

    @Test
    @DisplayName("si el ETag coincide se responde 304 sin cuerpo")
    void revalida_con_etag() throws Exception {
        MvcResult first = mockMvc().perform(get("/api/v1/parameters/groups"))
                .andExpect(status().isOk())
                .andReturn();

        String etag = first.getResponse().getHeader(HttpHeaders.ETAG);
        assertThat(etag).isNotBlank();
        assertThat(first.getResponse().getContentAsString()).isNotEmpty();

        MvcResult revalidated = mockMvc()
                .perform(get("/api/v1/parameters/groups").header(HttpHeaders.IF_NONE_MATCH, etag))
                .andExpect(status().isNotModified())
                .andReturn();

        assertThat(revalidated.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    @DisplayName("editar la configuracion cambia el ETag y fuerza una descarga nueva")
    void una_escritura_invalida_el_etag() throws Exception {
        String before = mockMvc().perform(get("/api/v1/brands"))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.ETAG);

        versions.bump(ResourceVersions.Family.BRANDS);

        mockMvc().perform(get("/api/v1/brands").header(HttpHeaders.IF_NONE_MATCH, before))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ETAG, org.hamcrest.Matchers.not(before)));
    }

    @Test
    @DisplayName("marcas y parametros tienen versiones independientes")
    void las_familias_son_independientes() {
        String parametersBefore = versions.etagOf(ResourceVersions.Family.PARAMETERS);
        String brandsBefore = versions.etagOf(ResourceVersions.Family.BRANDS);

        versions.bump(ResourceVersions.Family.PARAMETERS);

        assertThat(versions.etagOf(ResourceVersions.Family.PARAMETERS)).isNotEqualTo(parametersBefore);
        assertThat(versions.etagOf(ResourceVersions.Family.BRANDS)).isEqualTo(brandsBefore);
    }
}
