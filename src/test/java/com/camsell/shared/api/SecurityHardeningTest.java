package com.camsell.shared.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "camsell.rate-limit.credential-attempts-per-minute=3",
        "spring.datasource.url=jdbc:h2:mem:camsell-hardening;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class SecurityHardeningTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .addFilter(rateLimitFilter)
                .build();
        // Las cubetas son compartidas entre pruebas del mismo contexto.
        rateLimitFilter.resetAll();
    }

    @Test
    @DisplayName("las respuestas llevan las cabeceras de endurecimiento")
    void expone_cabeceras_de_seguridad() throws Exception {
        mockMvc.perform(get("/api/v1/listings"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().string("Cross-Origin-Opener-Policy", "same-origin"))
                .andExpect(header().string("Permissions-Policy", containsString("geolocation=()")));
    }

    @Test
    @DisplayName("probar claves en serie termina en 429 con Retry-After")
    void frena_la_fuerza_bruta_en_el_login() throws Exception {
        String payload = """
                {"email":"admin@camsell.dev","password":"clave-incorrecta"}
                """;

        for (int attempt = 0; attempt < 3; attempt++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isUnprocessableEntity());
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.code").value("rate_limited"))
                .andExpect(jsonPath("$.retryAfterSeconds").isNumber());
    }

    @Test
    @DisplayName("el limite de credenciales no afecta a las lecturas publicas")
    void las_lecturas_no_se_limitan() throws Exception {
        for (int attempt = 0; attempt < 12; attempt++) {
            mockMvc.perform(get("/api/v1/parameters/groups")).andExpect(status().isOk());
        }
    }
}
