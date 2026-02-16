package com.example.contentplatform.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.contentplatform.kafka.config.KafkaTopicConfig.ARTICLE_TOPIC;

@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void publishBatch() {

        List<OutboxEventEntity> events =
                repository.findNextBatch(
                        OutboxStatus.PENDING,
                        PageRequest.of(0, 10)
                );

        for (OutboxEventEntity event : events) {
            try {
                kafkaTemplate.send(
                        ARTICLE_TOPIC,
                        event.getAggregateId(),
                        event.getPayload()
                );
                event.markSent();
            } catch (Exception e) {
                event.markFailed();
            }
        }
    }
}
