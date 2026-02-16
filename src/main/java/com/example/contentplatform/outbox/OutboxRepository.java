package com.example.contentplatform.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select e from OutboxEventEntity e
        where e.status = :status
        order by e.createdAt
        """)
    List<OutboxEventEntity> findNextBatch(
            OutboxStatus status,
            Pageable pageable
    );
}
