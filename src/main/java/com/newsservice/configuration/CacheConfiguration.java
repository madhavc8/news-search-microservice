package com.newsservice.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration using Caffeine for high-performance caching
 * Supports multiple cache configurations for different use cases
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        // Configure different cache specifications for different cache names
        cacheManager.registerCustomCache("newsSearch", 
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .recordStats()
                .build());
                
        cacheManager.registerCustomCache("offlineCache",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(6, TimeUnit.HOURS)
                .recordStats()
                .build());
                
        cacheManager.registerCustomCache("healthCheck",
            Caffeine.newBuilder()
                .maximumSize(10)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build());

        return cacheManager;
    }
}
