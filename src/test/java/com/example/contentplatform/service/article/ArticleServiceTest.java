package com.example.contentplatform.service.article;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.events.ArticleEvent;
import com.example.contentplatform.events.ArticleEventType;
import com.example.contentplatform.kafka.producer.ArticleEventProducer;
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
    private ArticleEventProducer eventProducer;
    private ArticleService service;

    @BeforeEach
    void setup() {
        repository = mock(ArticleRepository.class);
        eventProducer = mock(ArticleEventProducer.class);
        service = new ArticleService(repository, eventProducer);
    }

    @Test
    void shouldCreateArticleAndPublishEvent() {
        // Given
        ArticleRequest request = new ArticleRequest("Title", "Body");

        ArticleEntity returnedEntity = new ArticleEntity("Title", "Body");
        setId(returnedEntity, 1L);

        when(repository.save(any(ArticleEntity.class))).thenReturn(returnedEntity);

        // When
        ArticleResponse response = service.create(request);

        // Then
        ArgumentCaptor<ArticleEntity> entityCaptor = ArgumentCaptor.forClass(ArticleEntity.class);
        verify(repository).save(entityCaptor.capture());
        ArticleEntity capturedEntity = entityCaptor.getValue();
        assertThat(capturedEntity.getTitle()).isEqualTo("Title");
        assertThat(capturedEntity.getContent()).isEqualTo("Body");

        ArgumentCaptor<ArticleEvent> eventCaptor = ArgumentCaptor.forClass(ArticleEvent.class);
        verify(eventProducer).publish(eventCaptor.capture());
        ArticleEvent event = eventCaptor.getValue();
        assertThat(event.type()).isEqualTo(ArticleEventType.CREATED);
        assertThat(event.articleId()).isEqualTo("1");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Title");
        assertThat(response.content()).isEqualTo("Body");
    }

    @Test
    void shouldUpdateArticleWhenExists() {
        // Given
        ArticleRequest request = new ArticleRequest("New Title", "New Body");
        ArticleEntity existing = new ArticleEntity("Old Title", "Old Body");
        setId(existing, 2L);

        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(repository.save(any(ArticleEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<ArticleResponse> updated = service.update(2L, request);

        // Then
        assertThat(updated).isPresent();
        assertThat(updated.get().title()).isEqualTo("New Title");
        assertThat(updated.get().content()).isEqualTo("New Body");

        verify(repository).save(existing);
        verify(eventProducer).publish(any(ArticleEvent.class));
    }

    @Test
    void shouldReturnEmptyWhenUpdateNonExisting() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<ArticleResponse> updated = service.update(99L, new ArticleRequest("T", "C"));

        // Then
        assertThat(updated).isEmpty();
        verify(repository, never()).save(any());
        verify(eventProducer, never()).publish(any());
    }

    @Test
    void shouldDeleteArticleWhenExists() {
        // Given
        ArticleEntity existing = new ArticleEntity("Title", "Body");
        setId(existing, 3L);
        when(repository.findById(3L)).thenReturn(Optional.of(existing));

        // When
        boolean deleted = service.delete(3L);

        // Then
        assertThat(deleted).isTrue();
        verify(repository).delete(existing);
        verify(eventProducer).publish(any(ArticleEvent.class));
    }

    @Test
    void shouldReturnFalseWhenDeleteNonExisting() {
        // Given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // When
        boolean deleted = service.delete(99L);

        // Then
        assertThat(deleted).isFalse();
        verify(repository, never()).delete(any());
        verify(eventProducer, never()).publish(any());
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

    // Utility method to set ID via reflection
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
