package com.example.contentplatform.integration;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.kafka.config.FakeKafkaConfig;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.kafka.listener.auto-startup=false"
)
@Testcontainers
@Import(FakeKafkaConfig.class)
class ArticleIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("content_platform")
                    .withUsername("cp_user")
                    .withPassword("cp_pass");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

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
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ArticleResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("Title", body.title());
        assertEquals("Body", body.content());
        assertEquals(1, repository.count());
    }

    @Test
    void shouldGetArticleById() {
        // Given
        ArticleResponse created = createArticle("Title", "Body");

        // When
        ResponseEntity<ArticleResponse> response =
                restTemplate.getForEntity("/articles/" + created.id(), ArticleResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Title", response.getBody().title());
        assertEquals("Body", response.getBody().content());
    }

    @Test
    void shouldUpdateArticle() {
        // Given
        ArticleResponse created = createArticle("Old Title", "Old Body");
        ArticleRequest updateRequest = new ArticleRequest("New Title", "New Body");

        // When
        restTemplate.put("/articles/" + created.id(), updateRequest);
        ResponseEntity<ArticleResponse> response =
                restTemplate.getForEntity("/articles/" + created.id(), ArticleResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Title", response.getBody().title());
        assertEquals("New Body", response.getBody().content());
    }

    @Test
    void shouldDeleteArticle() {
        // Given
        ArticleResponse created = createArticle("Title", "Body");

        // When
        restTemplate.delete("/articles/" + created.id());
        ResponseEntity<ArticleResponse> response =
                restTemplate.getForEntity("/articles/" + created.id(), ArticleResponse.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(0, repository.count());
    }

    // Utility method
    private ArticleResponse createArticle(String title, String content) {
        ArticleRequest request = new ArticleRequest(title, content);
        ResponseEntity<ArticleResponse> response =
                restTemplate.postForEntity("/articles", request, ArticleResponse.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody();
    }
}
