package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.config.RuntimeFeatureFlags;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiContentRouter {

    private final OllamaContentService ollamaService;
    private final NoOpAiContentService noOpService;
    private final RuntimeFeatureFlags flags;

    public AiContentRouter(
            OllamaContentService ollamaService,
            NoOpAiContentService noOpService,
            RuntimeFeatureFlags flags
    ) {
        this.ollamaService = ollamaService;
        this.noOpService = noOpService;
        this.flags = flags;
    }

    public List<ArticleResponse> search(String query) {
        return flags.isAiSearchEnabled()
                ? ollamaService.search(query)
                : noOpService.search(query);
    }
}
