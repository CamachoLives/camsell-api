package com.camsell.news.application;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.identity.domain.port.in.UserUseCase;
import com.camsell.news.domain.model.Article;
import com.camsell.news.domain.model.ArticleSearchQuery;
import com.camsell.news.domain.model.ArticleStatus;
import com.camsell.news.domain.port.in.ArticleUseCase;
import com.camsell.news.domain.port.out.ArticleRepositoryPort;
import com.camsell.parameters.domain.model.ParameterGroups;
import com.camsell.parameters.domain.port.in.ParameterQueryUseCase;
import com.camsell.shared.domain.exception.ResourceNotFoundException;
import com.camsell.shared.domain.page.PageQuery;
import com.camsell.shared.domain.page.PageResult;
import com.camsell.shared.domain.util.Slugs;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Contenido editorial. La categoria de cada nota se valida contra el grupo de
 * parametros {@code NEWS_CATEGORY}, por lo que las secciones del portal se
 * administran sin desplegar codigo.
 */
@Service
@Transactional
public class ArticleService implements ArticleUseCase {

    private final ArticleRepositoryPort articles;
    private final ParameterQueryUseCase parameters;
    private final CurrentUserPort currentUser;
    private final UserUseCase users;

    public ArticleService(
            ArticleRepositoryPort articles,
            ParameterQueryUseCase parameters,
            CurrentUserPort currentUser,
            UserUseCase users) {
        this.articles = articles;
        this.parameters = parameters;
        this.currentUser = currentUser;
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Article> search(ArticleSearchQuery query, PageQuery page) {
        return articles.search(query, page);
    }

    @Override
    public Article getBySlug(String slug) {
        Article article = articles.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", slug));
        if (!article.status().isPubliclyVisible() && !currentUser.isAdmin()) {
            throw new ResourceNotFoundException("Nota", slug);
        }
        articles.incrementViewCount(article.id());
        return article;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> featured(int limit) {
        return articles.findFeatured(limit <= 0 ? 3 : Math.min(limit, 10));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> related(String slug, int limit) {
        Article article = articles.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Nota", slug));
        return articles.findRelated(article.categoryCode(), article.id(), limit <= 0 ? 3 : Math.min(limit, 10));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> tags() {
        return articles.findAllTags();
    }

    @Override
    public Article create(SaveArticleCommand command) {
        validateCategory(command.categoryCode());
        User author = users.getById(currentUser.requireCurrentUserId());

        Article draft = new Article(
                null,
                generateSlug(command.title()),
                command.title().trim(),
                command.excerpt(),
                command.body(),
                command.coverImageUrl(),
                command.categoryCode(),
                normalizeTags(command.tags()),
                author.id(),
                author.fullName(),
                ArticleStatus.DRAFT,
                command.featured(),
                Article.estimateReadingMinutes(command.body()),
                0L,
                null,
                null);

        if (command.publishNow()) {
            draft.validateForPublication();
            draft = draft.withStatus(ArticleStatus.PUBLISHED, Instant.now());
        }
        return articles.save(draft);
    }

    @Override
    public Article update(Long id, SaveArticleCommand command) {
        Article current = requireArticle(id);
        validateCategory(command.categoryCode());

        Article updated = new Article(
                current.id(),
                current.slug(),
                command.title().trim(),
                command.excerpt(),
                command.body(),
                command.coverImageUrl(),
                command.categoryCode(),
                normalizeTags(command.tags()),
                current.authorId(),
                current.authorName(),
                current.status(),
                command.featured(),
                Article.estimateReadingMinutes(command.body()),
                current.viewCount(),
                current.publishedAt(),
                current.createdAt());

        if (command.publishNow() || current.status().isPubliclyVisible()) {
            updated.validateForPublication();
            updated = updated.withStatus(ArticleStatus.PUBLISHED, Instant.now());
        }
        return articles.save(updated);
    }

    @Override
    public Article changeStatus(Long id, ArticleStatus status) {
        Article current = requireArticle(id);
        if (status.isPubliclyVisible()) {
            current.validateForPublication();
        }
        return articles.save(current.withStatus(status, Instant.now()));
    }

    @Override
    public void delete(Long id) {
        requireArticle(id);
        articles.deleteById(id);
    }

    private Article requireArticle(Long id) {
        return articles.findById(id).orElseThrow(() -> new ResourceNotFoundException("Nota", id));
    }

    private void validateCategory(String categoryCode) {
        if (categoryCode != null && !categoryCode.isBlank()) {
            parameters.requireValidOption(ParameterGroups.NEWS_CATEGORY, categoryCode);
        }
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        return tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(tag -> tag.trim().toLowerCase())
                .distinct()
                .limit(8)
                .toList();
    }

    private String generateSlug(String title) {
        String base = Slugs.of(title);
        if (!articles.existsBySlug(base)) {
            return base;
        }
        for (int suffix = 2; suffix < 50; suffix++) {
            String candidate = base + "-" + suffix;
            if (!articles.existsBySlug(candidate)) {
                return candidate;
            }
        }
        return base + "-" + System.currentTimeMillis();
    }
}
