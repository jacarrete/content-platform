package com.example.contentplatform.outbox;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant sentAt;

    protected OutboxEventEntity() {}

    private OutboxEventEntity(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload
    ) {
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static OutboxEventEntity pending(
            String aggregateType,
            String aggregateId,
            String eventType,
            String payload
    ) {
        return new OutboxEventEntity(
                aggregateType,
                aggregateId,
                eventType,
                payload
        );
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
        this.sentAt = Instant.now();
    }

    public void markFailed() {
        this.status = OutboxStatus.FAILED;
    }
}
