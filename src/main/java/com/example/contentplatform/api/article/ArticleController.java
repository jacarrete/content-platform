package com.example.contentplatform.api.article;

import com.example.contentplatform.service.article.ArticleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ArticleResponse> getAll(Pageable pageable) {
        return service.getAll(pageable);
    }

    @PostMapping
    public ArticleResponse create(@Valid @RequestBody ArticleRequest request) {
        return service.create(request.title(), request.content());
    }
}
