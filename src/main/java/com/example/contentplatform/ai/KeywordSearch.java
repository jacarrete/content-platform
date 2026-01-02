package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.repository.article.ArticleMapper;
import com.example.contentplatform.repository.article.ArticleRepository;

import java.util.Arrays;
import java.util.List;

final class KeywordSearch {

    private KeywordSearch() {}

    static List<ArticleResponse> search(ArticleRepository repository, String query) {

        var keywords = query.toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .split("\\s+");

        return repository.findAll().stream()
                .filter(article ->
                        Arrays.stream(keywords)
                                .anyMatch(k ->
                                        article.getTitle().toLowerCase().contains(k)
                                                || article.getContent().toLowerCase().contains(k)
                                )
                )
                .map(ArticleMapper::toResponse)
                .toList();
    }
}
