package com.newsservice.domain.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Domain model representing the response from news search
 * Groups articles by time intervals as per requirements
 */
public class NewsSearchResponse {
    
    private String keyword;
    private Integer intervalValue;
    private TimeInterval intervalUnit;
    private LocalDateTime searchTimestamp;
    private Boolean fromCache;
    private Map<String, IntervalGroup> intervalGroups;
    private Integer totalArticles;
    private String status;
    private String message;

    // Default constructor
    public NewsSearchResponse() {
        this.searchTimestamp = LocalDateTime.now();
        this.fromCache = false;
        this.status = "success";
    }

    public NewsSearchResponse(String keyword, Integer intervalValue, TimeInterval intervalUnit) {
        this();
        this.keyword = keyword;
        this.intervalValue = intervalValue;
        this.intervalUnit = intervalUnit;
    }

    // Getters and Setters
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getIntervalValue() {
        return intervalValue;
    }

    public void setIntervalValue(Integer intervalValue) {
        this.intervalValue = intervalValue;
    }

    public TimeInterval getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(TimeInterval intervalUnit) {
        this.intervalUnit = intervalUnit;
    }

    public LocalDateTime getSearchTimestamp() {
        return searchTimestamp;
    }

    public void setSearchTimestamp(LocalDateTime searchTimestamp) {
        this.searchTimestamp = searchTimestamp;
    }

    public Boolean getFromCache() {
        return fromCache;
    }

    public void setFromCache(Boolean fromCache) {
        this.fromCache = fromCache;
    }

    public Map<String, IntervalGroup> getIntervalGroups() {
        return intervalGroups;
    }

    public void setIntervalGroups(Map<String, IntervalGroup> intervalGroups) {
        this.intervalGroups = intervalGroups;
    }

    public Integer getTotalArticles() {
        return totalArticles;
    }

    public void setTotalArticles(Integer totalArticles) {
        this.totalArticles = totalArticles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Inner class representing a group of articles within a time interval
     */
    public static class IntervalGroup {
        private String intervalLabel;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer count;
        private List<NewsArticle> articles;

        public IntervalGroup() {}

        public IntervalGroup(String intervalLabel, LocalDateTime startTime, LocalDateTime endTime, 
                           Integer count, List<NewsArticle> articles) {
            this.intervalLabel = intervalLabel;
            this.startTime = startTime;
            this.endTime = endTime;
            this.count = count;
            this.articles = articles;
        }

        public String getIntervalLabel() {
            return intervalLabel;
        }

        public void setIntervalLabel(String intervalLabel) {
            this.intervalLabel = intervalLabel;
        }

        public LocalDateTime getStartTime() {
            return startTime;
        }

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
        }

        public LocalDateTime getEndTime() {
            return endTime;
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public List<NewsArticle> getArticles() {
            return articles;
        }

        public void setArticles(List<NewsArticle> articles) {
            this.articles = articles;
        }

        @Override
        public String toString() {
            return "IntervalGroup{" +
                    "intervalLabel='" + intervalLabel + '\'' +
                    ", count=" + count +
                    ", startTime=" + startTime +
                    ", endTime=" + endTime +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "NewsSearchResponse{" +
                "keyword='" + keyword + '\'' +
                ", intervalValue=" + intervalValue +
                ", intervalUnit=" + intervalUnit +
                ", totalArticles=" + totalArticles +
                ", fromCache=" + fromCache +
                ", status='" + status + '\'' +
                '}';
    }
}
