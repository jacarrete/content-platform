package com.example.contentplatform.service.article;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.repository.article.ArticleEntity;
import com.example.contentplatform.repository.article.ArticleMapper;
import com.example.contentplatform.repository.article.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ArticleService {

    private final ArticleRepository repository;

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public Page<ArticleResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ArticleMapper::toResponse);
    }

    public ArticleResponse create(String title, String content) {
        return ArticleMapper.toResponse(
                repository.save(new ArticleEntity(title, content))
        );
    }
}
