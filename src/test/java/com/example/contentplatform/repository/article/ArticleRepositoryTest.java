package com.example.contentplatform.repository.article;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class ArticleRepositoryTest {

    @Autowired
    ArticleRepository repository;

    @Test
    void shouldPersistAndLoadArticle() {
        // Given
        final var article = new ArticleEntity("Title", "Body");

        // When
        repository.save(article);

        // Then
        Optional<ArticleEntity> found = repository.findById(article.getId());

        assertTrue(found.isPresent());
        assertEquals("Title", found.get().getTitle());
    }
}
