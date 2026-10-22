package com.camsell.news.domain.port.in;

import com.camsell.news.domain.model.Article;
import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.model.ArticleStatus;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;

import java.util.List;

public interface ArticleUseCase {

    PageResult<Article> search(ArticleSearchQuery query, PageQuery page);

    Article getBySlug(String slug);

    List<Article> featured(int limit);

    List<Article> related(String slug, int limit);

    List<String> tags();

    Article create(SaveArticleCommand command);

    Article update(Long id, SaveArticleCommand command);

    Article changeStatus(Long id, ArticleStatus status);

    void delete(Long id);

    record SaveArticleCommand(
            String title,
            String excerpt,
            String body,
            String coverImageUrl,
            String categoryCode,
            List<String> tags,
            boolean featured,
            boolean publishNow) {
    }
}
