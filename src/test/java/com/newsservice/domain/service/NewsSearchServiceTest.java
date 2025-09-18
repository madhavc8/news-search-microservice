package com.newsservice.domain.service;

import com.newsservice.domain.model.*;
import com.newsservice.infrastructure.external.NewsApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NewsSearchService
 * Tests core business logic and error handling
 */
@ExtendWith(MockitoExtension.class)
class NewsSearchServiceTest {

    @Mock
    private NewsApiClient newsApiClient;

    @Mock
    private OfflineModeService offlineModeService;

    @Mock
    private DateGroupingService dateGroupingService;

    private NewsSearchService newsSearchService;

    @BeforeEach
    void setUp() {
        newsSearchService = new NewsSearchService(
                newsApiClient, 
                offlineModeService, 
                dateGroupingService, 
                false // offline mode disabled for tests
        );
    }

    @Test
    void searchNews_WithValidRequest_ShouldReturnGroupedResults() {
        // Given
        NewsSearchRequest request = NewsSearchRequest.builder()
                .keyword("apple")
                .intervalValue(12)
                .intervalUnit(TimeInterval.HOURS)
                .offlineMode(false)
                .build();

        List<NewsArticle> mockArticles = createMockArticles();
        Map<String, NewsSearchResponse.IntervalGroup> mockGroups = createMockGroups();

        when(newsApiClient.searchNews(eq("apple"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Mono.just(mockArticles));
        when(dateGroupingService.groupArticlesByInterval(eq(mockArticles), eq(12), eq(TimeInterval.HOURS)))
                .thenReturn(mockGroups);

        // When & Then
        StepVerifier.create(newsSearchService.searchNews(request))
                .assertNext(response -> {
                    assertThat(response.getKeyword()).isEqualTo("apple");
                    assertThat(response.getIntervalValue()).isEqualTo(12);
                    assertThat(response.getIntervalUnit()).isEqualTo(TimeInterval.HOURS);
                    assertThat(response.getTotalArticles()).isEqualTo(2);
                    assertThat(response.getFromCache()).isFalse();
                    assertThat(response.getIntervalGroups()).isEqualTo(mockGroups);
                    assertThat(response.getStatus()).isEqualTo("success");
                })
                .verifyComplete();
    }

    @Test
    void searchNews_WithOfflineMode_ShouldUseCachedResults() {
        // Given
        NewsSearchRequest request = NewsSearchRequest.builder()
                .keyword("apple")
                .intervalValue(6)
                .intervalUnit(TimeInterval.HOURS)
                .offlineMode(true)
                .build();

        List<NewsArticle> cachedArticles = createMockArticles();
        Map<String, NewsSearchResponse.IntervalGroup> mockGroups = createMockGroups();

        when(offlineModeService.getCachedResults("apple"))
                .thenReturn(cachedArticles);
        when(dateGroupingService.groupArticlesByInterval(eq(cachedArticles), eq(6), eq(TimeInterval.HOURS)))
                .thenReturn(mockGroups);

        // When & Then
        StepVerifier.create(newsSearchService.searchNews(request))
                .assertNext(response -> {
                    assertThat(response.getKeyword()).isEqualTo("apple");
                    assertThat(response.getFromCache()).isTrue();
                    assertThat(response.getMessage()).contains("offline cache");
                })
                .verifyComplete();
    }

    @Test
    void searchNews_WithApiFailure_ShouldFallbackToOffline() {
        // Given
        NewsSearchRequest request = NewsSearchRequest.builder()
                .keyword("apple")
                .offlineMode(false)
                .build();

        List<NewsArticle> cachedArticles = createMockArticles();
        Map<String, NewsSearchResponse.IntervalGroup> mockGroups = createMockGroups();

        when(newsApiClient.searchNews(eq("apple"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Mono.error(new RuntimeException("API unavailable")));
        when(offlineModeService.getCachedResults("apple"))
                .thenReturn(cachedArticles);
        when(dateGroupingService.groupArticlesByInterval(any(), any(), any()))
                .thenReturn(mockGroups);

        // When & Then
        StepVerifier.create(newsSearchService.searchNews(request))
                .assertNext(response -> {
                    assertThat(response.getFromCache()).isTrue();
                    assertThat(response.getMessage()).contains("offline cache");
                })
                .verifyComplete();
    }

    @Test
    void searchNews_WithDefaultValues_ShouldApplyDefaults() {
        // Given
        NewsSearchRequest request = new NewsSearchRequest();
        request.setKeyword("technology");

        List<NewsArticle> mockArticles = createMockArticles();
        Map<String, NewsSearchResponse.IntervalGroup> mockGroups = createMockGroups();

        when(newsApiClient.searchNews(eq("technology"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Mono.just(mockArticles));
        when(dateGroupingService.groupArticlesByInterval(any(), eq(12), eq(TimeInterval.HOURS)))
                .thenReturn(mockGroups);

        // When & Then
        StepVerifier.create(newsSearchService.searchNews(request))
                .assertNext(response -> {
                    assertThat(response.getIntervalValue()).isEqualTo(12);
                    assertThat(response.getIntervalUnit()).isEqualTo(TimeInterval.HOURS);
                })
                .verifyComplete();
    }

    @Test
    void getServiceHealth_ShouldReturnHealthStatus() {
        // Given
        when(newsApiClient.isServiceAvailable()).thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(newsSearchService.getServiceHealth())
                .assertNext(health -> {
                    assertThat(health).containsKey("newsApiAvailable");
                    assertThat(health).containsKey("offlineModeEnabled");
                    assertThat(health).containsKey("timestamp");
                    assertThat(health).containsKey("status");
                    assertThat(health.get("newsApiAvailable")).isEqualTo(true);
                    assertThat(health.get("status")).isEqualTo("UP");
                })
                .verifyComplete();
    }

    private List<NewsArticle> createMockArticles() {
        NewsArticle article1 = new NewsArticle();
        article1.setTitle("Apple announces new iPhone");
        article1.setDescription("Apple has announced a new iPhone model");
        article1.setUrl("https://example.com/apple-iphone");
        article1.setPublishedAt(LocalDateTime.now().minusHours(2));
        
        NewsArticle.Source source1 = new NewsArticle.Source();
        source1.setName("Tech News");
        article1.setSource(source1);

        NewsArticle article2 = new NewsArticle();
        article2.setTitle("Apple stock rises");
        article2.setDescription("Apple stock price increases after earnings");
        article2.setUrl("https://example.com/apple-stock");
        article2.setPublishedAt(LocalDateTime.now().minusHours(5));
        
        NewsArticle.Source source2 = new NewsArticle.Source();
        source2.setName("Financial Times");
        article2.setSource(source2);

        return Arrays.asList(article1, article2);
    }

    private Map<String, NewsSearchResponse.IntervalGroup> createMockGroups() {
        NewsSearchResponse.IntervalGroup group = new NewsSearchResponse.IntervalGroup();
        group.setIntervalLabel("Last 12 hours");
        group.setCount(2);
        group.setArticles(createMockArticles());
        group.setStartTime(LocalDateTime.now().minusHours(12));
        group.setEndTime(LocalDateTime.now());

        return Map.of("Last 12 hours", group);
    }
}
