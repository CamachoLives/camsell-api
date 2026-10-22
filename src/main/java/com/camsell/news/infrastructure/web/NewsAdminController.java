package com.camsell.news.infrastructure.web;

import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.model.ArticleStatus;
import com.camsell.news.domain.port.in.ArticleUseCase;
import com.camsell.news.infrastructure.web.dto.ArticleDtos;
import com.camsell.shared.api.dto.PageResponse;
import com.camsell.shared.domain.page.PageQuery;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.EnumSet;

/** Redaccion editorial: solo administradores. */
@RestController
@RequestMapping("/api/v1/admin/news")
@PreAuthorize("hasRole('ADMIN')")
public class NewsAdminController {

    private final ArticleUseCase articles;

    public NewsAdminController(ArticleUseCase articles) {
        this.articles = articles;
    }

    @GetMapping
    public PageResponse<ArticleDtos.Summary> list(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String categoryCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        ArticleSearchQuery query = new ArticleSearchQuery(
                text, categoryCode, null, EnumSet.allOf(ArticleStatus.class), null);
        return PageResponse.from(articles
                .search(query, PageQuery.of(page, size, "createdAt", "desc"))
                .map(ArticleDtos.Summary::from));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ArticleDtos.Detail create(@Valid @RequestBody ArticleDtos.Save payload) {
        return ArticleDtos.Detail.from(articles.create(toCommand(payload)));
    }

    @PutMapping("/{id}")
    public ArticleDtos.Detail update(@PathVariable Long id, @Valid @RequestBody ArticleDtos.Save payload) {
        return ArticleDtos.Detail.from(articles.update(id, toCommand(payload)));
    }

    @PatchMapping("/{id}/status")
    public ArticleDtos.Detail changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ArticleDtos.ChangeStatus payload) {
        return ArticleDtos.Detail.from(articles.changeStatus(id, payload.status()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        articles.delete(id);
    }

    private ArticleUseCase.SaveArticleCommand toCommand(ArticleDtos.Save payload) {
        return new ArticleUseCase.SaveArticleCommand(
                payload.title(),
                payload.excerpt(),
                payload.body(),
                payload.coverImageUrl(),
                payload.categoryCode(),
                payload.tags(),
                payload.featured(),
                payload.publishNow());
    }
}
