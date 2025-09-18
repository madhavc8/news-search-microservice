package com.newsservice.domain.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Domain model representing a news search request
 * Includes validation annotations for input validation
 */
public class NewsSearchRequest {
    
    @NotBlank(message = "Keyword is required")
    private String keyword;
    
    @Positive(message = "Interval value must be positive")
    private Integer intervalValue;
    
    private TimeInterval intervalUnit;
    
    private Boolean offlineMode;

    // Default constructor
    public NewsSearchRequest() {}

    public NewsSearchRequest(String keyword, Integer intervalValue, TimeInterval intervalUnit, Boolean offlineMode) {
        this.keyword = keyword;
        this.intervalValue = intervalValue;
        this.intervalUnit = intervalUnit;
        this.offlineMode = offlineMode;
    }

    // Builder pattern for easier object creation
    public static Builder builder() {
        return new Builder();
    }

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

    public Boolean getOfflineMode() {
        return offlineMode;
    }

    public void setOfflineMode(Boolean offlineMode) {
        this.offlineMode = offlineMode;
    }

    /**
     * Apply default values as per requirements
     */
    public void applyDefaults() {
        if (intervalValue == null) {
            intervalValue = 12;
        }
        if (intervalUnit == null) {
            intervalUnit = TimeInterval.HOURS;
        }
        if (offlineMode == null) {
            offlineMode = false;
        }
    }

    @Override
    public String toString() {
        return "NewsSearchRequest{" +
                "keyword='" + keyword + '\'' +
                ", intervalValue=" + intervalValue +
                ", intervalUnit=" + intervalUnit +
                ", offlineMode=" + offlineMode +
                '}';
    }

    /**
     * Builder class for NewsSearchRequest
     */
    public static class Builder {
        private String keyword;
        private Integer intervalValue;
        private TimeInterval intervalUnit;
        private Boolean offlineMode;

        public Builder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public Builder intervalValue(Integer intervalValue) {
            this.intervalValue = intervalValue;
            return this;
        }

        public Builder intervalUnit(TimeInterval intervalUnit) {
            this.intervalUnit = intervalUnit;
            return this;
        }

        public Builder offlineMode(Boolean offlineMode) {
            this.offlineMode = offlineMode;
            return this;
        }

        public NewsSearchRequest build() {
            NewsSearchRequest request = new NewsSearchRequest(keyword, intervalValue, intervalUnit, offlineMode);
            request.applyDefaults();
            return request;
        }
    }
}
