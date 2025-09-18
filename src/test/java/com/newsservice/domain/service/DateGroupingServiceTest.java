package com.newsservice.domain.service;

import com.newsservice.domain.model.NewsArticle;
import com.newsservice.domain.model.NewsSearchResponse;
import com.newsservice.domain.model.TimeInterval;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DateGroupingService
 * Tests date-based grouping logic for different time intervals
 */
class DateGroupingServiceTest {

    private DateGroupingService dateGroupingService;

    @BeforeEach
    void setUp() {
        dateGroupingService = new DateGroupingService();
    }

    @Test
    void groupArticlesByInterval_WithHourlyInterval_ShouldGroupCorrectly() {
        // Given
        List<NewsArticle> articles = createTestArticles();
        
        // When
        Map<String, NewsSearchResponse.IntervalGroup> groups = 
                dateGroupingService.groupArticlesByInterval(articles, 6, TimeInterval.HOURS);
        
        // Then
        assertThat(groups).isNotEmpty();
        assertThat(groups.values().stream().mapToInt(g -> g.getCount()).sum()).isEqualTo(4);
        
        // Verify articles are grouped by time
        groups.values().forEach(group -> {
            assertThat(group.getArticles()).isNotEmpty();
            assertThat(group.getIntervalLabel()).contains("hours");
            assertThat(group.getStartTime()).isBefore(group.getEndTime());
        });
    }

    @Test
    void groupArticlesByInterval_WithDailyInterval_ShouldGroupCorrectly() {
        // Given
        List<NewsArticle> articles = createTestArticles();
        
        // When
        Map<String, NewsSearchResponse.IntervalGroup> groups = 
                dateGroupingService.groupArticlesByInterval(articles, 1, TimeInterval.DAYS);
        
        // Then
        assertThat(groups).isNotEmpty();
        groups.values().forEach(group -> {
            assertThat(group.getIntervalLabel()).contains("days");
            assertThat(group.getCount()).isGreaterThan(0);
        });
    }

    @Test
    void groupArticlesByInterval_WithEmptyList_ShouldReturnEmptyMap() {
        // Given
        List<NewsArticle> articles = Arrays.asList();
        
        // When
        Map<String, NewsSearchResponse.IntervalGroup> groups = 
                dateGroupingService.groupArticlesByInterval(articles, 12, TimeInterval.HOURS);
        
        // Then
        assertThat(groups).isEmpty();
    }

    @Test
    void groupArticlesByInterval_WithNullArticles_ShouldReturnEmptyMap() {
        // When
        Map<String, NewsSearchResponse.IntervalGroup> groups = 
                dateGroupingService.groupArticlesByInterval(null, 12, TimeInterval.HOURS);
        
        // Then
        assertThat(groups).isEmpty();
    }

    @Test
    void groupArticlesByInterval_WithWeeklyInterval_ShouldGroupCorrectly() {
        // Given
        List<NewsArticle> articles = createTestArticlesSpanningWeeks();
        
        // When
        Map<String, NewsSearchResponse.IntervalGroup> groups = 
                dateGroupingService.groupArticlesByInterval(articles, 1, TimeInterval.WEEKS);
        
        // Then
        assertThat(groups).isNotEmpty();
        groups.values().forEach(group -> {
            assertThat(group.getIntervalLabel()).contains("weeks");
            assertThat(group.getArticles()).allMatch(article -> 
                article.getPublishedAt().isAfter(group.getStartTime()) &&
                article.getPublishedAt().isBefore(group.getEndTime())
            );
        });
    }

    @Test
    void groupArticlesByInterval_WithMonthlyInterval_ShouldGroupCorrectly() {
        // Given
        List<NewsArticle> articles = createTestArticlesSpanningMonths();
        
        // When
        Map<String, NewsSearchResponse.IntervalGroup> groups = 
                dateGroupingService.groupArticlesByInterval(articles, 1, TimeInterval.MONTHS);
        
        // Then
        assertThat(groups).isNotEmpty();
        groups.values().forEach(group -> {
            assertThat(group.getIntervalLabel()).contains("months");
        });
    }

    private List<NewsArticle> createTestArticles() {
        LocalDateTime now = LocalDateTime.now();
        
        NewsArticle article1 = createArticle("Article 1", now.minusHours(1));
        NewsArticle article2 = createArticle("Article 2", now.minusHours(3));
        NewsArticle article3 = createArticle("Article 3", now.minusHours(8));
        NewsArticle article4 = createArticle("Article 4", now.minusHours(15));
        
        return Arrays.asList(article1, article2, article3, article4);
    }

    private List<NewsArticle> createTestArticlesSpanningWeeks() {
        LocalDateTime now = LocalDateTime.now();
        
        NewsArticle article1 = createArticle("Article 1", now.minusDays(2));
        NewsArticle article2 = createArticle("Article 2", now.minusDays(8));
        NewsArticle article3 = createArticle("Article 3", now.minusDays(15));
        
        return Arrays.asList(article1, article2, article3);
    }

    private List<NewsArticle> createTestArticlesSpanningMonths() {
        LocalDateTime now = LocalDateTime.now();
        
        NewsArticle article1 = createArticle("Article 1", now.minusDays(15));
        NewsArticle article2 = createArticle("Article 2", now.minusDays(45));
        NewsArticle article3 = createArticle("Article 3", now.minusDays(75));
        
        return Arrays.asList(article1, article2, article3);
    }

    private NewsArticle createArticle(String title, LocalDateTime publishedAt) {
        NewsArticle article = new NewsArticle();
        article.setTitle(title);
        article.setDescription("Test description for " + title);
        article.setUrl("https://example.com/" + title.toLowerCase().replace(" ", "-"));
        article.setPublishedAt(publishedAt);
        
        NewsArticle.Source source = new NewsArticle.Source();
        source.setName("Test Source");
        article.setSource(source);
        
        return article;
    }
}
