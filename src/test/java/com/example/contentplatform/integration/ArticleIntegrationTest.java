package com.example.contentplatform.integration;

import com.example.contentplatform.api.article.ArticleRequest;
import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update"); // automatically create tables
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ArticleRepository repository;

    @Test
    void shouldCreateAndFetchArticle() {
        // Clean DB before test
        repository.deleteAll();

        ArticleRequest request = new ArticleRequest("Title", "Body");

        ResponseEntity<ArticleResponse> response =
                restTemplate.postForEntity("/articles", request, ArticleResponse.class);

        // Check created response
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Title", response.getBody().title());
        assertEquals("Body", response.getBody().content());

        String location = Objects.requireNonNull(response.getHeaders().getLocation()).toString();
        assertNotNull(location);

        // Fetch the article from returned location
        ResponseEntity<ArticleResponse> fetched =
                restTemplate.getForEntity(location, ArticleResponse.class);

        assertEquals(HttpStatus.OK, fetched.getStatusCode());
        assertNotNull(fetched.getBody());
        assertEquals("Title", fetched.getBody().title());
        assertEquals("Body", fetched.getBody().content());

        // Also verify DB directly
        assertEquals(1, repository.count());
        assertTrue(repository.findById(fetched.getBody().id()).isPresent());
    }
}
