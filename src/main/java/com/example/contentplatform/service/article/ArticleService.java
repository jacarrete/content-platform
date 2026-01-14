package com.example.contentplatform.service.article;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.events.ArticleCreatedEvent;
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
    public ArticleResponse create(String title, String content) {
        final var article = ArticleMapper.toResponse(repository.save(new ArticleEntity(title, content)));
        articleEventProducer.publish(
                new ArticleCreatedEvent(
                        String.valueOf(article.id()),
                        article.title(),
                        article.content(),
                        Instant.now()
                )
        );
        return article;
    }
}
