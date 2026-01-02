package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public final class NoOpAiContentService implements AiContentService {

    private final ArticleRepository repository;

    public NoOpAiContentService(ArticleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ArticleResponse> search(String query) {
        return KeywordSearch.search(repository, query);
    }
}
