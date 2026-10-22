package com.camsell.news.infrastructure.web;

import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.port.in.ArticleUseCase;
import com.camsell.news.infrastructure.web.dto.ArticleDtos;
import com.camsell.shared.api.dto.PageResponse;
import com.camsell.shared.domain.page.PageQuery;
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
    public PageResponse<ArticleDtos.Summary> feed(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction) {
        return PageResponse.from(articles
                .search(
                        ArticleSearchQuery.publicFeed(text, categoryCode, tag),
                        PageQuery.of(page, size, sort, direction))
                .map(ArticleDtos.Summary::from));
    }

    @GetMapping("/featured")
    public List<ArticleDtos.Summary> featured(@RequestParam(defaultValue = "3") int limit) {
        return articles.featured(limit).stream().map(ArticleDtos.Summary::from).toList();
    }

    @GetMapping("/tags")
    public List<String> tags() {
        return articles.tags();
    }

    @GetMapping("/{slug}")
    public ArticleDtos.Detail detail(@PathVariable String slug) {
        return ArticleDtos.Detail.from(articles.getBySlug(slug));
    }

    @GetMapping("/{slug}/related")
    public List<ArticleDtos.Summary> related(
            @PathVariable String slug,
            @RequestParam(defaultValue = "3") int limit) {
        return articles.related(slug, limit).stream().map(ArticleDtos.Summary::from).toList();
    }
}
