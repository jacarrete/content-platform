package com.example.contentplatform.kafka.consumer;

import com.example.contentplatform.events.ArticleCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArticleEventConsumer {

    @KafkaListener(
            topics = "content.article.events",
            groupId = "content-platform",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ArticleCreatedEvent event) {
        try {
            log.info("[Kafka Consumer] Received event: {}", event);
        } catch (Exception e) {
            log.error("[Kafka Consumer] Failed processing event: {}", event, e);
            throw e;
        }
    }
}
