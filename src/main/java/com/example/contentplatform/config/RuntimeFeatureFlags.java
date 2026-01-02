package com.example.contentplatform.config;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RuntimeFeatureFlags {

    private final AtomicBoolean aiSearchEnabled = new AtomicBoolean(true);

    public boolean isAiSearchEnabled() {
        return aiSearchEnabled.get();
    }

    public void setAiSearchEnabled(boolean enabled) {
        aiSearchEnabled.set(enabled);
    }
}
