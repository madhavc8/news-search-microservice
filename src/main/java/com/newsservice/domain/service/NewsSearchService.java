package com.newsservice.domain.service;

import com.newsservice.domain.model.*;
import com.newsservice.infrastructure.external.NewsApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business service for news search functionality
 * Implements SOLID principles and handles offline mode
 */
@Service
public class NewsSearchService {

    private final NewsApiClient newsApiClient;
    private final OfflineModeService offlineModeService;
    private final DateGroupingService dateGroupingService;
    private final boolean offlineModeEnabled;

    public NewsSearchService(NewsApiClient newsApiClient,
                           OfflineModeService offlineModeService,
                           DateGroupingService dateGroupingService,
                           @Value("${offline-mode.enabled}") boolean offlineModeEnabled) {
        this.newsApiClient = newsApiClient;
        this.offlineModeService = offlineModeService;
        this.dateGroupingService = dateGroupingService;
        this.offlineModeEnabled = offlineModeEnabled;
    }

    /**
     * Search for news articles and group them by time intervals
     * Supports both online and offline modes
     */
    public Mono<NewsSearchResponse> searchNews(NewsSearchRequest request) {
        // Apply defaults if not provided
        request.applyDefaults();
        
        // Determine if we should use offline mode
        boolean useOfflineMode = shouldUseOfflineMode(request);
        
        if (useOfflineMode) {
            return searchOffline(request);
        } else {
            return searchOnline(request)
                    .onErrorResume(throwable -> {
                        // Fallback to offline mode if online search fails
                        return searchOffline(request);
                    });
        }
    }

    /**
     * Search news online using NewsAPI
     */
    @Cacheable(value = "newsSearch", key = "#request.keyword + '_' + #request.intervalValue + '_' + #request.intervalUnit")
    private Mono<NewsSearchResponse> searchOnline(NewsSearchRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime searchFrom = calculateSearchFromTime(now, request.getIntervalValue(), request.getIntervalUnit());
        
        return newsApiClient.searchNews(request.getKeyword(), searchFrom, now)
                .map(articles -> {
                    NewsSearchResponse response = new NewsSearchResponse(
                            request.getKeyword(), 
                            request.getIntervalValue(), 
                            request.getIntervalUnit()
                    );
                    
                    // Group articles by time intervals
                    Map<String, NewsSearchResponse.IntervalGroup> groups = 
                            dateGroupingService.groupArticlesByInterval(articles, request.getIntervalValue(), request.getIntervalUnit());
                    
                    response.setIntervalGroups(groups);
                    response.setTotalArticles(articles.size());
                    response.setFromCache(false);
                    response.setMessage("Results fetched from NewsAPI");
                    
                    // Cache results for offline mode
                    offlineModeService.cacheSearchResults(request.getKeyword(), articles);
                    
                    return response;
                })
                .onErrorMap(throwable -> new NewsSearchException("Failed to search news online", throwable));
    }

    /**
     * Search news from cached/offline data
     */
    private Mono<NewsSearchResponse> searchOffline(NewsSearchRequest request) {
        return Mono.fromCallable(() -> {
            List<NewsArticle> cachedArticles = offlineModeService.getCachedResults(request.getKeyword());
            
            if (cachedArticles.isEmpty()) {
                // Return sample data if no cached results available
                cachedArticles = generateSampleData(request.getKeyword());
            }
            
            NewsSearchResponse response = new NewsSearchResponse(
                    request.getKeyword(), 
                    request.getIntervalValue(), 
                    request.getIntervalUnit()
            );
            
            // Group articles by time intervals
            Map<String, NewsSearchResponse.IntervalGroup> groups = 
                    dateGroupingService.groupArticlesByInterval(cachedArticles, request.getIntervalValue(), request.getIntervalUnit());
            
            response.setIntervalGroups(groups);
            response.setTotalArticles(cachedArticles.size());
            response.setFromCache(true);
            response.setMessage("Results from offline cache");
            
            return response;
        });
    }

    /**
     * Check service health and availability
     */
    public Mono<Map<String, Object>> getServiceHealth() {
        return newsApiClient.isServiceAvailable()
                .map(available -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put("newsApiAvailable", available);
                    health.put("offlineModeEnabled", offlineModeEnabled);
                    health.put("timestamp", LocalDateTime.now());
                    health.put("status", available ? "UP" : "DEGRADED");
                    return health;
                });
    }

    /**
     * Determine if offline mode should be used
     */
    private boolean shouldUseOfflineMode(NewsSearchRequest request) {
        return Boolean.TRUE.equals(request.getOfflineMode()) || 
               (offlineModeEnabled && Boolean.TRUE.equals(request.getOfflineMode()));
    }

    /**
     * Calculate the start time for news search based on interval
     */
    private LocalDateTime calculateSearchFromTime(LocalDateTime now, Integer intervalValue, TimeInterval intervalUnit) {
        return switch (intervalUnit) {
            case MINUTES -> now.minus(intervalValue, ChronoUnit.MINUTES);
            case HOURS -> now.minus(intervalValue, ChronoUnit.HOURS);
            case DAYS -> now.minus(intervalValue, ChronoUnit.DAYS);
            case WEEKS -> now.minus(intervalValue * 7, ChronoUnit.DAYS);
            case MONTHS -> now.minus(intervalValue, ChronoUnit.MONTHS);
            case YEARS -> now.minus(intervalValue, ChronoUnit.YEARS);
        };
    }

    /**
     * Generate sample data for demonstration when no cached data is available
     */
    private List<NewsArticle> generateSampleData(String keyword) {
        List<NewsArticle> sampleArticles = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 10; i++) {
            NewsArticle article = new NewsArticle();
            article.setTitle("Sample " + keyword + " news article " + (i + 1));
            article.setDescription("This is a sample news article about " + keyword + " for offline demonstration.");
            article.setContent("Sample content for " + keyword + " article " + (i + 1));
            article.setUrl("https://example.com/news/" + (i + 1));
            article.setUrlToImage("https://example.com/images/" + (i + 1) + ".jpg");
            article.setPublishedAt(now.minus(i * 2, ChronoUnit.HOURS));
            article.setAuthor("Sample Author " + (i + 1));
            
            NewsArticle.Source source = new NewsArticle.Source();
            source.setId("sample-source");
            source.setName("Sample News Source");
            article.setSource(source);
            
            sampleArticles.add(article);
        }
        
        return sampleArticles;
    }

    /**
     * Custom exception for news search operations
     */
    public static class NewsSearchException extends RuntimeException {
        public NewsSearchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
