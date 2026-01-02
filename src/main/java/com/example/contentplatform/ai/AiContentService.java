package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;

import java.util.List;

public sealed interface AiContentService permits OllamaContentService, NoOpAiContentService {

    List<ArticleResponse> search(String query);
}
