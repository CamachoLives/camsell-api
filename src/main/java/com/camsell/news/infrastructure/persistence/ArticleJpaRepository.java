package com.camsell.news.infrastructure.persistence;

import com.camsell.news.domain.model.ArticleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleJpaRepository
        extends JpaRepository<ArticleEntity, Long>, JpaSpecificationExecutor<ArticleEntity> {

    Optional<ArticleEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<ArticleEntity> findByFeaturedTrueAndStatusOrderByPublishedAtDesc(
            ArticleStatus status, Pageable pageable);

    List<ArticleEntity> findByCategoryCodeAndStatusAndIdNotOrderByPublishedAtDesc(
            String categoryCode, ArticleStatus status, Long excludedId, Pageable pageable);

    @Query("select distinct tag from ArticleEntity a join a.tags tag "
            + "where a.status = com.camsell.news.domain.model.ArticleStatus.PUBLISHED order by tag")
    List<String> findAllPublishedTags();

    @Modifying
    @Query("update ArticleEntity a set a.viewCount = a.viewCount + 1 where a.id = :id")
    void incrementViewCount(@Param("id") Long id);
}
