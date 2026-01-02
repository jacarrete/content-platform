package com.example.contentplatform.api.feature;

import com.example.contentplatform.config.RuntimeFeatureFlags;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/features")
public class FeatureFlagController {

    private final RuntimeFeatureFlags featureFlags;

    public FeatureFlagController(RuntimeFeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @PostMapping("/ai-search/{enabled}")
    public void toggleAiSearch(@PathVariable boolean enabled) {
        featureFlags.setAiSearchEnabled(enabled);
    }

    @GetMapping("/ai-search")
    public boolean isAiSearchEnabled() {
        return featureFlags.isAiSearchEnabled();
    }
}
