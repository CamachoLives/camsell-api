package com.camsell.news.domain.model;

public enum ArticleStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public boolean isPubliclyVisible() {
        return this == PUBLISHED;
    }
}
