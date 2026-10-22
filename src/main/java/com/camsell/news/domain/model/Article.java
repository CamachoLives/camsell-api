package com.camsell.news.domain.model;

import com.camsell.shared.domain.exception.BusinessRuleException;

import java.time.Instant;
import java.util.List;

/**
 * Nota editorial del portal. Es la seccion informativa del sitio: lanzamientos,
 * pruebas de manejo, guias de compra y novedades del mercado.
 */
public record Article(
        Long id,
        String slug,
        String title,
        String excerpt,
        String body,
        String coverImageUrl,
        String categoryCode,
        List<String> tags,
        Long authorId,
        String authorName,
        ArticleStatus status,
        boolean featured,
        int readingMinutes,
        long viewCount,
        Instant publishedAt,
        Instant createdAt) {

    private static final int WORDS_PER_MINUTE = 200;

    public Article {
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public void validateForPublication() {
        if (title == null || title.isBlank()) {
            throw new BusinessRuleException("El titulo de la nota es obligatorio");
        }
        if (body == null || body.length() < 200) {
            throw new BusinessRuleException(
                    "short_article_body",
                    "El cuerpo de la nota debe tener al menos 200 caracteres para publicarse");
        }
        if (coverImageUrl == null || coverImageUrl.isBlank()) {
            throw new BusinessRuleException(
                    "article_without_cover",
                    "La nota necesita una imagen de portada");
        }
    }

    /** Estimacion del tiempo de lectura mostrada en la tarjeta de la nota. */
    public static int estimateReadingMinutes(String body) {
        if (body == null || body.isBlank()) {
            return 1;
        }
        int words = body.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil((double) words / WORDS_PER_MINUTE));
    }

    public Article withStatus(ArticleStatus newStatus, Instant now) {
        Instant published = newStatus == ArticleStatus.PUBLISHED && publishedAt == null ? now : publishedAt;
        return new Article(id, slug, title, excerpt, body, coverImageUrl, categoryCode, tags, authorId,
                authorName, newStatus, featured, readingMinutes, viewCount, published, createdAt);
    }
}
