package com.example.contentplatform.api.article;

import jakarta.validation.constraints.NotBlank;

public record ArticleRequest(
        @NotBlank String title,
        @NotBlank String content
) {}
