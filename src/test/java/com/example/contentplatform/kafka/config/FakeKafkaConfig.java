package com.example.contentplatform.kafka.config;

import com.example.contentplatform.kafka.producer.ArticleEventProducer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.example.contentplatform.events.ArticleEvent;

@TestConfiguration
public class FakeKafkaConfig {

    @Bean
    public ArticleEventProducer articleEventProducer() {
        return new ArticleEventProducer(null) {
            @Override
            public void publish(ArticleEvent event) {
                // do nothing
            }
        };
    }
}

