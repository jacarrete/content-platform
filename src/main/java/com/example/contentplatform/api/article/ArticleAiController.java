package com.example.contentplatform.api.article;

import com.example.contentplatform.ai.AiContentRouter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/articles/search/ai")
public class ArticleAiController {

    private final AiContentRouter router;

    public ArticleAiController(AiContentRouter router) {
        this.router = router;
    }

    @GetMapping
    public List<ArticleResponse> search(@RequestParam String query) {
        return router.search(query);
    }
}
