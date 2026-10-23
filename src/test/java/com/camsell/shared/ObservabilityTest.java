package com.camsell.shared;

import com.camsell.shared.api.RequestCorrelationFilter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:camsell-obs;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class ObservabilityTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MeterRegistry registry;

    @Autowired
    private RequestCorrelationFilter correlationFilter;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(correlationFilter)
                .build();
    }

    @Test
    @DisplayName("cada respuesta devuelve el identificador de correlacion")
    void devuelve_el_request_id() throws Exception {
        mockMvc().perform(get("/api/v1/listings"))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestCorrelationFilter.HEADER));
    }

    @Test
    @DisplayName("si el cliente envia un request id, se respeta y se devuelve igual")
    void respeta_el_request_id_del_cliente() throws Exception {
        mockMvc().perform(get("/api/v1/listings").header(RequestCorrelationFilter.HEADER, "trace-abc"))
                .andExpect(header().string(RequestCorrelationFilter.HEADER, "trace-abc"));
    }

    @Test
    @DisplayName("la busqueda del catalogo publica su latencia")
    void mide_la_latencia_de_la_busqueda() throws Exception {
        mockMvc().perform(get("/api/v1/listings")).andExpect(status().isOk());

        var timer = registry.find("camsell.catalog.search").timer();

        assertThat(timer).as("el temporizador del caso de uso debe existir").isNotNull();
        assertThat(timer.count()).isPositive();
    }

    @Test
    @DisplayName("los intentos de inicio de sesion se cuentan, exitosos y fallidos")
    void cuenta_los_intentos_de_login() throws Exception {
        mockMvc().perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@camsell.dev","password":"Camsell2026*"}
                                """))
                .andExpect(status().isOk());

        var counters = registry.find("camsell.auth.logins").counters();

        assertThat(counters).isNotEmpty();
        assertThat(counters.stream().mapToDouble(counter -> counter.count()).sum()).isPositive();
    }

    @Test
    @DisplayName("las metricas quedan etiquetadas con la aplicacion")
    void etiqueta_las_metricas() {
        var meter = registry.find("jvm.memory.used").gauge();

        assertThat(meter).isNotNull();
        assertThat(meter.getId().getTag("application")).isEqualTo("camsell-api");
    }

    @Test
    @DisplayName("el contador de peticiones lentas esta registrado desde el arranque")
    void registra_el_contador_de_lentitud() {
        assertThat(registry.find("camsell.http.slow_requests").counter()).isNotNull();
    }
}
