package com.example.contentplatform.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ARTICLE_TOPIC = "content.article.events";
    public static final String ARTICLE_TOPIC_DLT = "content.article.events.dlt";

    @Bean
    public NewTopic articleTopic() {
        return TopicBuilder.name(ARTICLE_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic articleTopicDLT() {
        return TopicBuilder.name(ARTICLE_TOPIC_DLT)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
