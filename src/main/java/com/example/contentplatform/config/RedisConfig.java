package com.example.contentplatform.config;

import com.example.contentplatform.api.article.ArticleResponseList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host}") String host,
            @Value("${spring.redis.port}") int port
    ) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, ArticleResponseList> redisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, ArticleResponseList> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Key as plain string
        template.setKeySerializer(new StringRedisSerializer());

        // Value as JSON, with type info included (safe for records / DTOs)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
