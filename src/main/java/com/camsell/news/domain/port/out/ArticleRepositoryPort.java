package com.camsell.news.domain.port.out;

import com.camsell.news.domain.model.Article;
import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;

import java.util.List;
import java.util.Optional;

public interface ArticleRepositoryPort {

    PageResult<Article> search(ArticleSearchQuery query, PageQuery page);

    Optional<Article> findBySlug(String slug);

    Optional<Article> findById(Long id);

    boolean existsBySlug(String slug);

    List<Article> findFeatured(int limit);

    List<Article> findRelated(String categoryCode, Long excludeId, int limit);

    List<String> findAllTags();

    Article save(Article article);

    void deleteById(Long id);

    void incrementViewCount(Long id);
}
