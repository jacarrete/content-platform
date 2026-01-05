package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.repository.article.ArticleMapper;
import com.example.contentplatform.repository.article.ArticleRepository;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public final class OllamaContentService implements AiContentService {

    private final ChatClient chatClient;
    private final ArticleRepository repository;
    private final ObservationRegistry registry;

    public OllamaContentService(
            ChatClient.Builder chatClientBuilder,
            ArticleRepository repository,
            ObservationRegistry registry
    ) {
        this.chatClient = chatClientBuilder.build();
        this.repository = repository;
        this.registry = registry;
    }

    @Override
    public List<ArticleResponse> search(String query) {
        return Observation.createNotStarted("ai.ollama.call", registry)
                .observe(() -> aiSearch(query));
    }

    private List<ArticleResponse> aiSearch(String query) {
        log.info("Searching articles via AI");
        var articles = repository.findAll();
        if (articles.isEmpty()) {
            return List.of();
        }

        var prompt = """
            You are a strict content retrieval assistant.
            ONLY return the IDs of articles that match the query.
            Output as a comma-separated list of numbers.
            NO text, labels, brackets, or extra characters.

            Query: %s

            Articles:
            %s
            """.formatted(
                query,
                articles.stream()
                        .map(a -> "%d|%s|%s".formatted(a.getId(), a.getTitle(), a.getContent()))
                        .toList()
        );

        try {
            var result = chatClient.prompt(prompt).call().content();
            var ids = parseIds(result);

            if (ids.isEmpty()) {
                return KeywordSearch.search(repository, query);
            }

            return articles.stream()
                    .filter(a -> ids.contains(a.getId()))
                    .map(ArticleMapper::toResponse)
                    .toList();

        } catch (Exception e) {
            log.warn("AI search failed, falling back to keyword search", e);
            return KeywordSearch.search(repository, query);
        }
    }

    private List<Long> parseIds(String result) {
        if (result == null || result.isBlank()) {
            return List.of();
        }
        return Stream.of(result.replaceAll("[^0-9,]", "").split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
    }
}
