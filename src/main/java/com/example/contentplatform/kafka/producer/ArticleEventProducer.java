package com.example.contentplatform.kafka.producer;

import com.example.contentplatform.events.ArticleEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ArticleEventProducer {

    private static final String TOPIC = "content.article.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ArticleEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ArticleEvent event) {
        kafkaTemplate.send(
                TOPIC,
                event.getArticleId(), // key
                event
        );
    }
}
