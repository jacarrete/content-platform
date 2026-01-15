package com.example.contentplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ContentPlatformApplication {

    static void main(String[] args) {
        SpringApplication.run(ContentPlatformApplication.class, args);
    }
}
