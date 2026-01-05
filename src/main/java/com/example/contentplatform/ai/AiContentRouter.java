package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.config.RuntimeFeatureFlags;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiContentRouter {

    private final OllamaContentService ollamaService;
    private final NoOpAiContentService noOpService;
    private final RuntimeFeatureFlags flags;
    private final ObservationRegistry registry;

    public AiContentRouter(
            OllamaContentService ollamaService,
            NoOpAiContentService noOpService,
            RuntimeFeatureFlags flags,
            ObservationRegistry registry
    ) {
        this.ollamaService = ollamaService;
        this.noOpService = noOpService;
        this.flags = flags;
        this.registry = registry;
    }

    public List<ArticleResponse> search(String query) {
        return Observation.createNotStarted("ai.search", registry)
                .lowCardinalityKeyValue("ai.enabled",
                        String.valueOf(flags.isAiSearchEnabled()))
                .observe(() ->
                        flags.isAiSearchEnabled()
                                ? ollamaService.search(query)
                                : noOpService.search(query)
                );
    }
}
