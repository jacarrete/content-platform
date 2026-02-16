package com.example.contentplatform.service.article;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.events.ArticleEvent;
import com.example.contentplatform.events.ArticleEventType;
import com.example.contentplatform.outbox.OutboxEventEntity;
import com.example.contentplatform.outbox.OutboxPayloadSerializer;
import com.example.contentplatform.outbox.OutboxRepository;
import com.example.contentplatform.repository.article.ArticleEntity;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ArticleServiceTest {

    private ArticleRepository repository;
    private OutboxRepository outboxRepository;
    private OutboxPayloadSerializer serializer;
    private ArticleService service;

    @BeforeEach
    void setup() {
        repository = mock(ArticleRepository.class);
        outboxRepository = mock(OutboxRepository.class);
        serializer = mock(OutboxPayloadSerializer.class);
        service = new ArticleService(repository, outboxRepository, serializer);
    }

    @Test
    void shouldCreateArticleAndWriteOutboxEvent() {
        // Given
        ArticleRequest request = new ArticleRequest("Title", "Body");

        ArticleEntity saved = new ArticleEntity("Title", "Body");
        setId(saved, 1L);

        when(repository.save(any(ArticleEntity.class))).thenReturn(saved);
        when(serializer.serialize(any(ArticleEvent.class))).thenReturn("{json}");

        // When
        ArticleResponse response = service.create(request);

        // Then
        verify(repository).save(any(ArticleEntity.class));
        verify(serializer).serialize(any(ArticleEvent.class));

        ArgumentCaptor<OutboxEventEntity> outboxCaptor =
                ArgumentCaptor.forClass(OutboxEventEntity.class);

        verify(outboxRepository).save(outboxCaptor.capture());

        OutboxEventEntity outbox = outboxCaptor.getValue();
        assertThat(outbox.getAggregateType()).isEqualTo("ARTICLE");
        assertThat(outbox.getAggregateId()).isEqualTo("1");
        assertThat(outbox.getEventType()).isEqualTo(ArticleEventType.CREATED.name());
        assertThat(outbox.getPayload()).isEqualTo("{json}");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.content()).isEqualTo("Body");
    }

    @Test
    void shouldUpdateArticleWhenExistsAndWriteOutboxEvent() {
        // Given
        ArticleEntity existing = new ArticleEntity("Old", "Old");
        setId(existing, 2L);

        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(serializer.serialize(any(ArticleEvent.class))).thenReturn("{json}");

        // When
        Optional<ArticleResponse> updated =
                service.update(2L, new ArticleRequest("New", "New"));

        // Then
        assertThat(updated).isPresent();
        assertThat(updated.get().title()).isEqualTo("New");
        assertThat(updated.get().content()).isEqualTo("New");

        verify(outboxRepository).save(any(OutboxEventEntity.class));
    }

    @Test
    void shouldReturnEmptyWhenUpdateNonExisting() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<ArticleResponse> result =
                service.update(99L, new ArticleRequest("T", "C"));

        // Then
        assertThat(result).isEmpty();
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void shouldDeleteArticleWhenExistsAndWriteOutboxEvent() {
        // Given
        ArticleEntity existing = new ArticleEntity("Title", "Body");
        setId(existing, 3L);

        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(serializer.serialize(any(ArticleEvent.class))).thenReturn("{json}");

        // When
        boolean deleted = service.delete(3L);

        // Then
        assertThat(deleted).isTrue();
        verify(repository).delete(existing);
        verify(outboxRepository).save(any(OutboxEventEntity.class));
    }

    @Test
    void shouldReturnFalseWhenDeleteNonExisting() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When
        boolean deleted = service.delete(99L);

        // Then
        assertThat(deleted).isFalse();
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void shouldGetArticleById() {
        // Given
        ArticleEntity entity = new ArticleEntity("Title", "Body");
        setId(entity, 4L);

        when(repository.findById(4L)).thenReturn(Optional.of(entity));

        // When
        Optional<ArticleResponse> response = service.getById(4L);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().id()).isEqualTo(4L);
    }

    @Test
    void shouldGetAllArticles() {
        // Given
        ArticleEntity e1 = new ArticleEntity("T1", "B1");
        ArticleEntity e2 = new ArticleEntity("T2", "B2");
        setId(e1, 5L);
        setId(e2, 6L);

        Page<ArticleEntity> page = new PageImpl<>(List.of(e1, e2));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        // When
        Page<ArticleResponse> result = service.getAll(Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).id()).isEqualTo(5L);
        assertThat(result.getContent().get(1).id()).isEqualTo(6L);
    }

    // Utility method for tests only
    private void setId(ArticleEntity entity, Long id) {
        try {
            var field = ArticleEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
