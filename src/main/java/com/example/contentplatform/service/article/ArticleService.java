package com.example.contentplatform.service.article;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.events.ArticleEvent;
import com.example.contentplatform.events.ArticleEventType;
import com.example.contentplatform.kafka.producer.ArticleEventProducer;
import com.example.contentplatform.repository.article.ArticleEntity;
import com.example.contentplatform.repository.article.ArticleMapper;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository repository;
    private final ArticleEventProducer articleEventProducer;

    public ArticleService(ArticleRepository repository, ArticleEventProducer articleEventProducer) {
        this.repository = repository;
        this.articleEventProducer = articleEventProducer;
    }

    @Transactional(readOnly = true)
    public Optional<ArticleResponse> getById(Long id) {
        return repository.findById(id)
                .map(article -> new ArticleResponse(article.getId(), article.getTitle(), article.getContent()));
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ArticleMapper::toResponse);
    }

    @Transactional
    public ArticleResponse create(ArticleRequest articleRequest) {
        final var articleSaved = repository.save(
                new ArticleEntity(articleRequest.title(), articleRequest.content())
        );

        articleEventProducer.publish(new ArticleEvent(
                String.valueOf(articleSaved.getId()),
                ArticleEventType.CREATED,
                Instant.now()
        ));

        return ArticleMapper.toResponse(articleSaved);
    }

    @Transactional
    public Optional<ArticleResponse> update(Long id, ArticleRequest request) {
        return repository.findById(id)
                .map(article -> {
                    article.update(request.title(), request.content());
                    final var articleUpdated = repository.save(article);
                    articleEventProducer.publish(new ArticleEvent(
                            String.valueOf(articleUpdated.getId()),
                            ArticleEventType.UPDATED,
                            Instant.now()
                    ));
                    return ArticleMapper.toResponse(articleUpdated);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return repository.findById(id)
                .map(article -> {
                    repository.delete(article);
                    articleEventProducer.publish(new ArticleEvent(
                            String.valueOf(article.getId()),
                            ArticleEventType.DELETED,
                            Instant.now()
                    ));
                    return true;
                })
                .orElse(false);
    }
}
