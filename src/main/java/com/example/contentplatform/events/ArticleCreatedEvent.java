package com.example.contentplatform.events;

import java.time.Instant;

public record ArticleCreatedEvent(
        String articleId,
        String title,
        String content,
        Instant occurredAt
) implements ArticleEvent {

    @Override
    public String getEventType() {
        return "ARTICLE_CREATED";
    }

    @Override
    public String getArticleId() {
        return this.articleId;
    }
}
