package com.example.contentplatform.api.error;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String path,
        Map<String, String> validationErrors
) {}
