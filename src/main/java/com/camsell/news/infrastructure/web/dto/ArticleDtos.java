package com.camsell.news.infrastructure.web.dto;

import com.camsell.news.domain.model.Article;
import com.camsell.news.domain.model.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public final class ArticleDtos {

    private ArticleDtos() {
    }

    /** Tarjeta del feed de noticias. */
    public record Summary(
            Long id,
            String slug,
            String title,
            String excerpt,
            String coverImageUrl,
            String categoryCode,
            List<String> tags,
            String authorName,
            ArticleStatus status,
            boolean featured,
            int readingMinutes,
            long viewCount,
            Instant publishedAt) {

        public static Summary from(Article article) {
            return new Summary(
                    article.id(),
                    article.slug(),
                    article.title(),
                    article.excerpt(),
                    article.coverImageUrl(),
                    article.categoryCode(),
                    article.tags(),
                    article.authorName(),
                    article.status(),
                    article.featured(),
                    article.readingMinutes(),
                    article.viewCount(),
                    article.publishedAt());
        }
    }

    public record Detail(
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

        public static Detail from(Article article) {
            return new Detail(
                    article.id(),
                    article.slug(),
                    article.title(),
                    article.excerpt(),
                    article.body(),
                    article.coverImageUrl(),
                    article.categoryCode(),
                    article.tags(),
                    article.authorId(),
                    article.authorName(),
                    article.status(),
                    article.featured(),
                    article.readingMinutes(),
                    article.viewCount(),
                    article.publishedAt(),
                    article.createdAt());
        }
    }

    public record Save(
            @NotBlank(message = "El titulo es obligatorio")
            @Size(max = 200)
            String title,

            @Size(max = 500, message = "El resumen admite maximo 500 caracteres")
            String excerpt,

            @NotBlank(message = "El cuerpo de la nota es obligatorio")
            @Size(max = 20000)
            String body,

            @Size(max = 500)
            String coverImageUrl,

            @Size(max = 60)
            String categoryCode,

            List<String> tags,

            boolean featured,

            boolean publishNow) {
    }

    public record ChangeStatus(
            @NotNull(message = "El estado es obligatorio")
            ArticleStatus status) {
    }
}
