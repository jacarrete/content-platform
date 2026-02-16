package com.example.contentplatform.api.article;

import com.example.contentplatform.service.article.ArticleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ArticleResponse>> getAll(
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        Page<ArticleResponse> page = service.getAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> getById(
            @PathVariable Long id
    ) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(
            @Valid @RequestBody ArticleRequest request
    ) {
        ArticleResponse response = service.create(request);
        URI location = URI.create("/articles/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request
    ) {
        boolean updated = service.update(id, request).isPresent();
        return updated
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
