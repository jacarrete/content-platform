package com.example.contentplatform.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record ArticleEvent(
        @JsonProperty("articleId") String articleId,
        @JsonProperty("type") ArticleEventType type,
        @JsonProperty("occurredAt") Instant occurredAt
) {
    @JsonCreator
    public ArticleEvent {}
}
