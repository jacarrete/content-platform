package com.example.contentplatform.api.article;

import java.io.Serializable;
import java.util.List;

public record ArticleResponseList(
        List<ArticleResponse> articles
) implements Serializable {}
