package com.example.contentplatform.integration;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.outbox.OutboxEventEntity;
import com.example.contentplatform.outbox.OutboxRepository;
import com.example.contentplatform.outbox.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.kafka.listener.auto-startup=false",
                "outbox.scheduler.enabled=false"
        }
)
class OutboxSchedulerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    OutboxRepository outboxRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldPersistOutboxEventWhenArticleIsCreated() {
        ArticleRequest request = new ArticleRequest("Title", "Body");

        var response = restTemplate.postForEntity(
                "/articles",
                request,
                Object.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        List<OutboxEventEntity> events = outboxRepository.findAll();
        assertThat(events).hasSize(1);

        OutboxEventEntity event = events.get(0);
        assertThat(event.getAggregateType()).isEqualTo("ARTICLE");
        assertThat(event.getEventType()).isEqualTo("CREATED");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getPayload()).isNotBlank();
    }
}
