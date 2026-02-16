package com.example.contentplatform.kafka.consumer;

import com.example.contentplatform.events.ArticleEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArticleEventConsumer {

    private final ObjectMapper objectMapper;

    public ArticleEventConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "content.article.events",
            groupId = "content-platform"
    )
    public void consume(String payload) throws Exception {
        ArticleEvent event =
                objectMapper.readValue(payload, ArticleEvent.class);

        log.info("[Kafka Consumer] Received event: {}", event);
    }
}
