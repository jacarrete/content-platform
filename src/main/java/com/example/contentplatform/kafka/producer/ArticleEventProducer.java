package com.example.contentplatform.kafka.producer;

import com.example.contentplatform.events.ArticleEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.example.contentplatform.kafka.config.KafkaTopicConfig.ARTICLE_TOPIC;

@Component
public class ArticleEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ArticleEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(ArticleEvent event) {
        kafkaTemplate.send(
                ARTICLE_TOPIC,
                event.articleId(), // key
                event
        );
    }
}
