package com.camsell.news.infrastructure.persistence;

import com.camsell.news.domain.model.Article;
import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.model.ArticleStatus;
import com.camsell.news.domain.port.out.ArticleRepositoryPort;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import com.camsell.shared.infrastructure.persistence.PageMappings;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Repository
public class ArticlePersistenceAdapter implements ArticleRepositoryPort {

    private static final Set<String> SORTABLE = Set.of("publishedAt", "title", "viewCount", "createdAt");

    private final ArticleJpaRepository repository;

    public ArticlePersistenceAdapter(ArticleJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<Article> search(ArticleSearchQuery query, PageQuery page) {
        return PageMappings.toPageResult(
                repository.findAll(toSpecification(query), PageMappings.toPageable(page, SORTABLE, "publishedAt")),
                this::toDomain);
    }

    @Override
    public Optional<Article> findBySlug(String slug) {
        return repository.findBySlug(slug).map(this::toDomain);
    }

    @Override
    public Optional<Article> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return repository.existsBySlug(slug);
    }

    @Override
    public List<Article> findFeatured(int limit) {
        return repository.findByFeaturedTrueAndStatusOrderByPublishedAtDesc(
                        ArticleStatus.PUBLISHED,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Article> findRelated(String categoryCode, Long excludeId, int limit) {
        if (categoryCode == null) {
            return List.of();
        }
        return repository.findByCategoryCodeAndStatusAndIdNotOrderByPublishedAtDesc(
                        categoryCode,
                        ArticleStatus.PUBLISHED,
                        excludeId,
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "publishedAt")))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<String> findAllTags() {
        return repository.findAllPublishedTags();
    }

    @Override
    public Article save(Article article) {
        ArticleEntity entity = article.id() == null
                ? new ArticleEntity()
                : repository.findById(article.id()).orElseGet(ArticleEntity::new);
        entity.setSlug(article.slug());
        entity.setTitle(article.title());
        entity.setExcerpt(article.excerpt());
        entity.setBody(article.body());
        entity.setCoverImageUrl(article.coverImageUrl());
        entity.setCategoryCode(article.categoryCode());
        entity.setAuthorId(article.authorId());
        entity.setAuthorName(article.authorName());
        entity.setStatus(article.status());
        entity.setFeatured(article.featured());
        entity.setReadingMinutes(article.readingMinutes());
        entity.setViewCount(article.viewCount());
        entity.setPublishedAt(article.publishedAt());
        entity.getTags().clear();
        entity.getTags().addAll(article.tags());
        return toDomain(repository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        repository.incrementViewCount(id);
    }

    private Specification<ArticleEntity> toSpecification(ArticleSearchQuery query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("status").in(query.statuses()));

            if (query.text() != null && !query.text().isBlank()) {
                String pattern = "%" + query.text().trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("excerpt")), pattern),
                        cb.like(cb.lower(root.get("body")), pattern)));
            }
            if (query.categoryCode() != null && !query.categoryCode().isBlank()) {
                predicates.add(cb.equal(root.get("categoryCode"), query.categoryCode()));
            }
            if (query.featured() != null) {
                predicates.add(cb.equal(root.get("featured"), query.featured()));
            }
            if (query.tag() != null && !query.tag().isBlank()) {
                predicates.add(cb.isMember(query.tag().toLowerCase(Locale.ROOT), root.get("tags")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Article toDomain(ArticleEntity entity) {
        return new Article(
                entity.getId(),
                entity.getSlug(),
                entity.getTitle(),
                entity.getExcerpt(),
                entity.getBody(),
                entity.getCoverImageUrl(),
                entity.getCategoryCode(),
                List.copyOf(entity.getTags()),
                entity.getAuthorId(),
                entity.getAuthorName(),
                entity.getStatus(),
                entity.isFeatured(),
                entity.getReadingMinutes(),
                entity.getViewCount(),
                entity.getPublishedAt(),
                entity.getCreatedAt());
    }
}
