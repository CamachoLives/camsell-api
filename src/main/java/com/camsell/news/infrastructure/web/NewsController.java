package com.camsell.news.infrastructure.web;

import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.port.in.ArticleUseCase;
import com.camsell.news.infrastructure.web.dto.ArticleDtos;
import com.camsell.shared.api.CachePolicy;
import com.camsell.shared.api.dto.PageResponse;
import com.camsell.shared.domain.page.PageQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Seccion informativa publica del portal. */
@RestController
@RequestMapping("/api/v1/news")
public class NewsController {

    private final ArticleUseCase articles;

    public NewsController(ArticleUseCase articles) {
        this.articles = articles;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ArticleDtos.Summary>> feed(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        return CachePolicy.publicResponse(
                PageResponse.from(articles
                        .search(
                                ArticleSearchQuery.publicFeed(text, categoryCode, tag),
                                PageQuery.of(page, size, sort, direction))
                        .map(ArticleDtos.Summary::from)),
                CachePolicy.FEED);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ArticleDtos.Summary>> featured(
            @RequestParam(defaultValue = "3") int limit) {
        return CachePolicy.publicResponse(
                articles.featured(limit).stream().map(ArticleDtos.Summary::from).toList(),
                CachePolicy.FEED);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<String>> tags() {
        return CachePolicy.publicResponse(articles.tags(), CachePolicy.CONFIGURATION);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ArticleDtos.Detail> detail(@PathVariable String slug) {
        return CachePolicy.publicResponse(
                ArticleDtos.Detail.from(articles.getBySlug(slug)), CachePolicy.DETAIL);
    }

    @GetMapping("/{slug}/related")
    public ResponseEntity<List<ArticleDtos.Summary>> related(
            @PathVariable String slug,
            @RequestParam(defaultValue = "3") int limit) {
        return CachePolicy.publicResponse(
                articles.related(slug, limit).stream().map(ArticleDtos.Summary::from).toList(),
                CachePolicy.DETAIL);
    }
}
