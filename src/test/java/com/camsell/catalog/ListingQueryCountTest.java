package com.camsell.catalog;

import com.camsell.catalog.domain.model.ListingSearchQuery;
import com.camsell.catalog.domain.port.out.ListingRepositoryPort;
import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.port.out.ArticleRepositoryPort;
import com.camsell.shared.domain.page.PageQuery;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Fija el coste en consultas de las lecturas de listado.
 *
 * <p>Sin estas cotas es facil reintroducir un N+1 sin notarlo: basta con mapear
 * el agregado completo en una busqueda para que cada resultado dispare una
 * consulta por cada coleccion perezosa.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "camsell.demo-data.enabled=true",
        "spring.jpa.properties.hibernate.generate_statistics=true",
        "spring.datasource.url=jdbc:h2:mem:camsell-queries;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
class ListingQueryCountTest {

    @Autowired
    private ListingRepositoryPort listings;

    @Autowired
    private ArticleRepositoryPort articles;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Statistics statistics;

    @BeforeEach
    void resetStatistics() {
        statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistics.clear();
    }

    private long queries() {
        return statistics.getPrepareStatementCount();
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("una pagina del catalogo no escala en consultas con el numero de resultados")
    void la_busqueda_no_tiene_n_mas_uno() {
        var page = listings.search(ListingSearchQuery.publicDefaults(), new PageQuery(0, 12, null, null));

        assertThat(page.content()).hasSizeGreaterThan(5);
        // Una consulta de datos, una de conteo y una por lote de portadas.
        assertThat(queries())
                .as("consultas para %d publicaciones", page.content().size())
                .isLessThanOrEqualTo(3);
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("la tarjeta no carga los atributos del vehiculo")
    void la_tarjeta_no_toca_los_atributos() {
        var page = listings.search(ListingSearchQuery.publicDefaults(), new PageQuery(0, 12, null, null));
        long afterSearch = queries();

        // Leer la tarjeta completa no dispara ninguna consulta extra: no hay
        // colecciones perezosas pendientes.
        page.content().forEach(card -> {
            card.coverImageUrl();
            card.price().amount();
            card.title();
        });

        assertThat(queries()).isEqualTo(afterSearch);
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("duplicar el tamano de pagina no duplica las consultas")
    void el_coste_es_constante_con_el_tamano_de_pagina() {
        listings.search(ListingSearchQuery.publicDefaults(), new PageQuery(0, 4, null, null));
        long withFour = queries();

        statistics.clear();
        listings.search(ListingSearchQuery.publicDefaults(), new PageQuery(0, 12, null, null));
        long withTwelve = queries();

        assertThat(withTwelve).isEqualTo(withFour);
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("el feed de noticias resuelve las etiquetas por lote")
    void el_feed_de_noticias_no_tiene_n_mas_uno() {
        var page = articles.search(
                ArticleSearchQuery.publicFeed(null, null, null), new PageQuery(0, 9, null, null));

        assertThat(page.content()).hasSizeGreaterThan(3);
        page.content().forEach(article -> assertThat(article.tags()).isNotNull());
        // Datos, conteo y un lote de etiquetas.
        assertThat(queries())
                .as("consultas para %d notas", page.content().size())
                .isLessThanOrEqualTo(3);
    }

    @Test
    @Transactional(readOnly = true)
    @DisplayName("la ficha completa si carga imagenes y atributos, en consultas acotadas")
    void la_ficha_carga_el_agregado_completo() {
        var listing = listings.findBySlug("toyota-corolla-cross-2024").orElseThrow();

        assertThat(listing.images()).isNotEmpty();
        assertThat(listing.attributes()).isNotEmpty();
        // La publicacion, sus imagenes y sus atributos.
        assertThat(queries()).isLessThanOrEqualTo(3);
    }
}
