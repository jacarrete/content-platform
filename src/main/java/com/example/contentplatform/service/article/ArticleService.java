package com.example.contentplatform.service.article;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.events.ArticleEvent;
import com.example.contentplatform.events.ArticleEventType;
import com.example.contentplatform.outbox.OutboxEventEntity;
import com.example.contentplatform.outbox.OutboxPayloadSerializer;
import com.example.contentplatform.outbox.OutboxRepository;
import com.example.contentplatform.repository.article.ArticleEntity;
import com.example.contentplatform.repository.article.ArticleMapper;
import com.example.contentplatform.repository.article.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository repository;
    private final OutboxRepository outboxRepository;
    private final OutboxPayloadSerializer serializer;

    @Transactional(readOnly = true)
    public Optional<ArticleResponse> getById(Long id) {
        return repository.findById(id)
                .map(ArticleMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ArticleMapper::toResponse);
    }

    @Transactional
    public ArticleResponse create(ArticleRequest request) {
        ArticleEntity saved = repository.save(new ArticleEntity(request.title(), request.content()));
        createOutboxEvent(saved.getId().toString(), ArticleEventType.CREATED);
        return ArticleMapper.toResponse(saved);
    }

    @Transactional
    public Optional<ArticleResponse> update(Long id, ArticleRequest request) {
        return repository.findById(id)
                .map(article -> {
                    article.update(request.title(), request.content());
                    createOutboxEvent(article.getId().toString(), ArticleEventType.UPDATED);
                    return ArticleMapper.toResponse(article);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.findById(id)
                .map(article -> {
                    createOutboxEvent(article.getId().toString(), ArticleEventType.DELETED);
                    repository.delete(article);
                    return true;
                })
                .orElse(false);
    }

    private void createOutboxEvent(String aggregateId, ArticleEventType type) {
        ArticleEvent event = new ArticleEvent(
                aggregateId,
                type,
                Instant.now()
        );

        outboxRepository.save(
                OutboxEventEntity.pending(
                        "ARTICLE",
                        aggregateId,
                        type.name(),
                        serializer.serialize(event)
                )
        );
    }
}
