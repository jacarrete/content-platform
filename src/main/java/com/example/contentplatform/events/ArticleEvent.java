package com.example.contentplatform.events;

import java.time.Instant;

public record ArticleEvent(
        String articleId,
        ArticleEventType type,
        Instant occurredAt
) {}
