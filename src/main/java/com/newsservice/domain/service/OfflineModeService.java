package com.newsservice.domain.service;

import com.newsservice.domain.model.NewsArticle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for handling offline mode functionality
 * Manages caching and fallback data for when NewsAPI is unavailable
 */
@Service
public class OfflineModeService {

    private final Map<String, CachedSearchResult> searchCache = new ConcurrentHashMap<>();
    private final Duration cacheDuration;

    public OfflineModeService(@Value("${offline-mode.cache-duration}") Duration cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    /**
     * Cache search results for offline access
     */
    public void cacheSearchResults(String keyword, List<NewsArticle> articles) {
        if (keyword != null && articles != null && !articles.isEmpty()) {
            CachedSearchResult cachedResult = new CachedSearchResult(
                    keyword.toLowerCase().trim(),
                    articles,
                    LocalDateTime.now()
            );
            searchCache.put(keyword.toLowerCase().trim(), cachedResult);
        }
    }

    /**
     * Retrieve cached search results
     */
    @Cacheable(value = "offlineCache", key = "#keyword.toLowerCase()")
    public List<NewsArticle> getCachedResults(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalizedKeyword = keyword.toLowerCase().trim();
        CachedSearchResult cachedResult = searchCache.get(normalizedKeyword);

        if (cachedResult != null && !isCacheExpired(cachedResult)) {
            return new ArrayList<>(cachedResult.getArticles());
        }

        // Try partial keyword matching
        return searchCache.entrySet().stream()
                .filter(entry -> !isCacheExpired(entry.getValue()))
                .filter(entry -> entry.getKey().contains(normalizedKeyword) || 
                               normalizedKeyword.contains(entry.getKey()))
                .findFirst()
                .<List<NewsArticle>>map(entry -> new ArrayList<>(entry.getValue().getArticles()))
                .orElse(Collections.emptyList());
    }

    /**
     * Get cache statistics for monitoring
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCachedKeywords", searchCache.size());
        stats.put("cacheDuration", cacheDuration.toString());
        
        long validEntries = searchCache.values().stream()
                .mapToLong(result -> isCacheExpired(result) ? 0 : 1)
                .sum();
        
        stats.put("validCachedEntries", validEntries);
        stats.put("expiredEntries", searchCache.size() - validEntries);
        
        List<String> cachedKeywords = new ArrayList<>(searchCache.keySet());
        stats.put("cachedKeywords", cachedKeywords);
        
        return stats;
    }

    /**
     * Clear expired cache entries
     */
    @Scheduled(fixedRate = 3600000) // Run every hour
    @CacheEvict(value = "offlineCache", allEntries = true)
    public void cleanupExpiredCache() {
        searchCache.entrySet().removeIf(entry -> isCacheExpired(entry.getValue()));
    }

    /**
     * Manually clear all cache
     */
    public void clearCache() {
        searchCache.clear();
    }

    /**
     * Check if a cached result has expired
     */
    private boolean isCacheExpired(CachedSearchResult cachedResult) {
        return cachedResult.getCachedAt().plus(cacheDuration).isBefore(LocalDateTime.now());
    }

    /**
     * Inner class to represent cached search results
     */
    private static class CachedSearchResult {
        private final String keyword;
        private final List<NewsArticle> articles;
        private final LocalDateTime cachedAt;

        public CachedSearchResult(String keyword, List<NewsArticle> articles, LocalDateTime cachedAt) {
            this.keyword = keyword;
            this.articles = new ArrayList<>(articles); // Defensive copy
            this.cachedAt = cachedAt;
        }

        public String getKeyword() {
            return keyword;
        }

        public List<NewsArticle> getArticles() {
            return articles;
        }

        public LocalDateTime getCachedAt() {
            return cachedAt;
        }

        @Override
        public String toString() {
            return "CachedSearchResult{" +
                    "keyword='" + keyword + '\'' +
                    ", articlesCount=" + articles.size() +
                    ", cachedAt=" + cachedAt +
                    '}';
        }
    }
}
