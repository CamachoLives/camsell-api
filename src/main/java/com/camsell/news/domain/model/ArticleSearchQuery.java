package com.camsell.news.domain.model;

import java.util.Set;

public record ArticleSearchQuery(
        String text,
        String categoryCode,
        String tag,
        Set<ArticleStatus> statuses,
        Boolean featured) {

    public ArticleSearchQuery {
        statuses = statuses == null || statuses.isEmpty() ? Set.of(ArticleStatus.PUBLISHED) : Set.copyOf(statuses);
    }

    public static ArticleSearchQuery publicFeed(String text, String categoryCode, String tag) {
        return new ArticleSearchQuery(text, categoryCode, tag, Set.of(ArticleStatus.PUBLISHED), null);
    }
}
