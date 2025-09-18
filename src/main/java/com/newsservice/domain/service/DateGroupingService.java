package com.newsservice.domain.service;

import com.newsservice.domain.model.NewsArticle;
import com.newsservice.domain.model.NewsSearchResponse;
import com.newsservice.domain.model.TimeInterval;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for grouping news articles by date intervals
 * Implements the Single Responsibility Principle
 */
@Service
public class DateGroupingService {

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Group articles by specified time intervals
     * 
     * @param articles List of news articles to group
     * @param intervalValue Number of time units per interval
     * @param intervalUnit Type of time unit (hours, days, etc.)
     * @return Map of interval groups with articles
     */
    public Map<String, NewsSearchResponse.IntervalGroup> groupArticlesByInterval(
            List<NewsArticle> articles, Integer intervalValue, TimeInterval intervalUnit) {
        
        if (articles == null || articles.isEmpty()) {
            return new LinkedHashMap<>();
        }

        // Sort articles by published date (newest first)
        List<NewsArticle> sortedArticles = articles.stream()
                .filter(article -> article.getPublishedAt() != null)
                .sorted((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                .collect(Collectors.toList());

        if (sortedArticles.isEmpty()) {
            return new LinkedHashMap<>();
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oldestArticleTime = sortedArticles.get(sortedArticles.size() - 1).getPublishedAt();
        
        // Create time intervals from now back to the oldest article
        List<TimeIntervalBucket> intervals = createTimeIntervals(now, oldestArticleTime, intervalValue, intervalUnit);
        
        // Group articles into intervals
        Map<String, NewsSearchResponse.IntervalGroup> groupedResults = new LinkedHashMap<>();
        
        for (TimeIntervalBucket interval : intervals) {
            List<NewsArticle> articlesInInterval = sortedArticles.stream()
                    .filter(article -> isArticleInInterval(article, interval))
                    .collect(Collectors.toList());
            
            if (!articlesInInterval.isEmpty() || shouldIncludeEmptyIntervals()) {
                NewsSearchResponse.IntervalGroup group = new NewsSearchResponse.IntervalGroup(
                        interval.getLabel(),
                        interval.getStartTime(),
                        interval.getEndTime(),
                        articlesInInterval.size(),
                        articlesInInterval
                );
                
                groupedResults.put(interval.getLabel(), group);
            }
        }
        
        return groupedResults;
    }

    /**
     * Create time interval buckets based on the specified interval
     */
    private List<TimeIntervalBucket> createTimeIntervals(LocalDateTime now, LocalDateTime oldestTime, 
                                                        Integer intervalValue, TimeInterval intervalUnit) {
        List<TimeIntervalBucket> intervals = new ArrayList<>();
        LocalDateTime currentEnd = now;
        
        while (currentEnd.isAfter(oldestTime)) {
            LocalDateTime currentStart = calculateIntervalStart(currentEnd, intervalValue, intervalUnit);
            
            // Don't go beyond the oldest article time
            if (currentStart.isBefore(oldestTime)) {
                currentStart = oldestTime;
            }
            
            String label = createIntervalLabel(currentStart, currentEnd, intervalUnit);
            intervals.add(new TimeIntervalBucket(label, currentStart, currentEnd));
            
            currentEnd = currentStart;
        }
        
        return intervals;
    }

    /**
     * Calculate the start time for an interval given the end time
     */
    private LocalDateTime calculateIntervalStart(LocalDateTime endTime, Integer intervalValue, TimeInterval intervalUnit) {
        return switch (intervalUnit) {
            case MINUTES -> endTime.minus(intervalValue, ChronoUnit.MINUTES);
            case HOURS -> endTime.minus(intervalValue, ChronoUnit.HOURS);
            case DAYS -> endTime.minus(intervalValue, ChronoUnit.DAYS);
            case WEEKS -> endTime.minus(intervalValue * 7, ChronoUnit.DAYS);
            case MONTHS -> endTime.minus(intervalValue, ChronoUnit.MONTHS);
            case YEARS -> endTime.minus(intervalValue, ChronoUnit.YEARS);
        };
    }

    /**
     * Create a human-readable label for the time interval
     */
    private String createIntervalLabel(LocalDateTime start, LocalDateTime end, TimeInterval intervalUnit) {
        return switch (intervalUnit) {
            case MINUTES -> String.format("Last %d minutes (%s - %s)", 
                    ChronoUnit.MINUTES.between(start, end),
                    start.format(DateTimeFormatter.ofPattern("HH:mm")),
                    end.format(DateTimeFormatter.ofPattern("HH:mm")));
            case HOURS -> String.format("Last %d hours (%s - %s)", 
                    ChronoUnit.HOURS.between(start, end),
                    start.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                    end.format(DateTimeFormatter.ofPattern("HH:mm")));
            case DAYS -> String.format("Last %d days (%s - %s)", 
                    ChronoUnit.DAYS.between(start, end),
                    start.format(DateTimeFormatter.ofPattern("MMM dd")),
                    end.format(DateTimeFormatter.ofPattern("MMM dd")));
            case WEEKS -> String.format("Last %d weeks (%s - %s)", 
                    ChronoUnit.DAYS.between(start, end) / 7,
                    start.format(DateTimeFormatter.ofPattern("MMM dd")),
                    end.format(DateTimeFormatter.ofPattern("MMM dd")));
            case MONTHS -> String.format("Last %d months (%s - %s)", 
                    ChronoUnit.MONTHS.between(start, end),
                    start.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    end.format(DateTimeFormatter.ofPattern("MMM yyyy")));
            case YEARS -> String.format("Last %d years (%s - %s)", 
                    ChronoUnit.YEARS.between(start, end),
                    start.format(DateTimeFormatter.ofPattern("yyyy")),
                    end.format(DateTimeFormatter.ofPattern("yyyy")));
        };
    }

    /**
     * Check if an article falls within a specific time interval
     */
    private boolean isArticleInInterval(NewsArticle article, TimeIntervalBucket interval) {
        LocalDateTime publishedAt = article.getPublishedAt();
        return publishedAt.isAfter(interval.getStartTime()) && 
               publishedAt.isBefore(interval.getEndTime()) ||
               publishedAt.equals(interval.getStartTime()) ||
               publishedAt.equals(interval.getEndTime());
    }

    /**
     * Determine if empty intervals should be included in results
     */
    private boolean shouldIncludeEmptyIntervals() {
        return false; // Only include intervals with articles for cleaner output
    }

    /**
     * Inner class representing a time interval bucket
     */
    private static class TimeIntervalBucket {
        private final String label;
        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        public TimeIntervalBucket(String label, LocalDateTime startTime, LocalDateTime endTime) {
            this.label = label;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getLabel() {
            return label;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        @Override
        public String toString() {
            return "TimeIntervalBucket{" +
                    "label='" + label + '\'' +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    '}';
        }
    }
}
