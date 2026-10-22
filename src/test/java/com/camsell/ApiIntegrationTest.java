package com.camsell;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica el contrato publico de la API sobre el contexto completo: Flyway,
 * mapeo de Hibernate, seguridad y datos de demostracion.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:camsell-it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class ApiIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("el esquema de parametros se expone publicamente con sus opciones")
    void expone_el_esquema_de_parametros() throws Exception {
        mockMvc().perform(get("/api/v1/parameters/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CONDITION"))
                .andExpect(jsonPath("$[0].dataType").value("OPTION"))
                .andExpect(jsonPath("$[0].scope").value("LISTING"))
                .andExpect(jsonPath("$[0].options.length()").value(3));
    }

    @Test
    @DisplayName("el arbol de marcas y modelos se consulta sin autenticacion")
    void expone_marcas_con_modelos() throws Exception {
        mockMvc().perform(get("/api/v1/brands").param("withModels", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].models").isArray());
    }

    @Test
    @DisplayName("la busqueda del catalogo devuelve una pagina con publicaciones visibles")
    void busca_el_catalogo() throws Exception {
        mockMvc().perform(get("/api/v1/listings").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    @DisplayName("el filtro por parametros usa el formato GRUPO:OPCION")
    void filtra_por_parametros_dinamicos() throws Exception {
        mockMvc().perform(get("/api/v1/listings").param("attr", "FUEL_TYPE:HYBRID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("el feed de noticias esta disponible al publico")
    void expone_el_feed_de_noticias() throws Exception {
        mockMvc().perform(get("/api/v1/news"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].readingMinutes").isNumber());
    }

    @Test
    @DisplayName("el login devuelve un access token y el perfil del usuario")
    void autentica_y_devuelve_token() throws Exception {
        mockMvc().perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@camsell.dev","password":"Camsell2026*"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.role").value("ADMIN"));
    }

    @Test
    @DisplayName("una clave incorrecta no revela si el correo existe")
    void rechaza_credenciales_invalidas() throws Exception {
        mockMvc().perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@camsell.dev","password":"clave-incorrecta"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("invalid_credentials"));
    }

    @Test
    @DisplayName("la administracion de parametros exige autenticacion")
    void protege_la_administracion() throws Exception {
        mockMvc().perform(get("/api/v1/admin/parameters/groups"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("publicar un vehiculo exige sesion")
    void protege_la_publicacion() throws Exception {
        mockMvc().perform(post("/api/v1/listings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("un payload mal formado responde 400 y no 500")
    void payload_invalido_es_400() throws Exception {
        mockMvc().perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ esto no es json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("malformed_payload"));
    }

    @Test
    @DisplayName("un registro sin campos obligatorios detalla los errores de validacion")
    void detalla_errores_de_validacion() throws Exception {
        mockMvc().perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"no-es-correo","password":"123","fullName":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_payload"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("los indicadores de la portada reflejan el catalogo cargado")
    void expone_indicadores_del_catalogo() throws Exception {
        mockMvc().perform(get("/api/v1/stats/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publishedListings").value(14))
                .andExpect(jsonPath("$.brandsWithListings").value(10));
    }
}
