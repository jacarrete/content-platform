package com.example.contentplatform.ai;

import com.example.contentplatform.api.article.ArticleResponse;
import com.example.contentplatform.api.article.ArticleResponseList;
import com.example.contentplatform.config.RuntimeFeatureFlags;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class AiContentRouter {

    private final OllamaContentService ollamaService;
    private final NoOpAiContentService noOpService;
    private final RuntimeFeatureFlags flags;
    private final ObservationRegistry registry;
    private final RedisTemplate<String, ArticleResponseList> redisTemplate;
    private final Duration cacheTtl = Duration.ofMinutes(5);

    public ArticleResponseList search(String query) {
        final var key = "ai-search:" + query;

        log.info("AI search executed for query: {}", query);

        // Try fetching from Redis first
        ValueOperations<String, ArticleResponseList> ops = redisTemplate.opsForValue();
        ArticleResponseList cached = ops.get(key);
        if (cached != null) {
            return cached;
        }

        // Compute result
        List<ArticleResponse> result = Observation.createNotStarted("ai.search", registry)
                .lowCardinalityKeyValue("ai.enabled", String.valueOf(flags.isAiSearchEnabled()))
                .observe(() ->
                        flags.isAiSearchEnabled()
                                ? ollamaService.search(query)
                                : noOpService.search(query)
                );

        ArticleResponseList wrapper = new ArticleResponseList(result);

        // Store in Redis with TTL
        ops.set(key, wrapper, cacheTtl);

        return wrapper;
    }
}
