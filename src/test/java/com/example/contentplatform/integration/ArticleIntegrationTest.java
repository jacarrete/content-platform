package com.example.contentplatform.integration;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.kafka.listener.auto-startup=false"
)
class ArticleIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ArticleRepository repository;

    @BeforeEach
    void cleanDb() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateArticle() {
        // Given
        ArticleRequest request = new ArticleRequest("Title", "Body");

        // When
        ResponseEntity<ArticleResponse> response =
                restTemplate.postForEntity("/articles", request, ArticleResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Title");
        assertThat(response.getBody().content()).isEqualTo("Body");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void shouldGetArticleById() {
        // Given
        ArticleResponse created = createArticle("Title", "Body");

        // When
        ResponseEntity<ArticleResponse> response =
                restTemplate.getForEntity("/articles/" + created.id(), ArticleResponse.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().title()).isEqualTo("Title");
        assertThat(response.getBody().content()).isEqualTo("Body");
    }

    @Test
    void shouldUpdateArticle() {
        // Given
        ArticleResponse created = createArticle("Old Title", "Old Body");
        ArticleRequest updateRequest = new ArticleRequest("New Title", "New Body");

        // When
        ResponseEntity<Void> updateResponse =
                restTemplate.exchange(
                        "/articles/" + created.id(),
                        org.springframework.http.HttpMethod.PUT,
                        new org.springframework.http.HttpEntity<>(updateRequest),
                        Void.class
                );

        // Then
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // When
        ResponseEntity<ArticleResponse> getResponse =
                restTemplate.getForEntity("/articles/" + created.id(), ArticleResponse.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().title()).isEqualTo("New Title");
        assertThat(getResponse.getBody().content()).isEqualTo("New Body");
    }

    @Test
    void shouldDeleteArticle() {
        // Given
        ArticleResponse created = createArticle("Title", "Body");

        // When
        ResponseEntity<Void> deleteResponse =
                restTemplate.exchange(
                        "/articles/" + created.id(),
                        org.springframework.http.HttpMethod.DELETE,
                        null,
                        Void.class
                );

        // Then
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(repository.count()).isEqualTo(0);

        // When
        ResponseEntity<ArticleResponse> getResponse =
                restTemplate.getForEntity("/articles/" + created.id(), ArticleResponse.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Utility method
    private ArticleResponse createArticle(String title, String content) {
        ArticleRequest request = new ArticleRequest(title, content);

        ResponseEntity<ArticleResponse> response =
                restTemplate.postForEntity("/articles", request, ArticleResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        return response.getBody();
    }
}
