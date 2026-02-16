package com.example.contentplatform.outbox;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class OutboxScheduler {

    private final OutboxPublisherService publisher;

    public OutboxScheduler(OutboxPublisherService publisher) {
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 5000)
    public void publish() {
        publisher.publishBatch();
    }
}
