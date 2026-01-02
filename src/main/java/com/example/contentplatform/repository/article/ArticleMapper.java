package com.example.contentplatform.repository.article;

import com.example.contentplatform.api.article.ArticleResponse;

public final class ArticleMapper {

    private ArticleMapper() {}

    public static ArticleResponse toResponse(ArticleEntity entity) {
        return new ArticleResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getContent()
        );
    }
}
