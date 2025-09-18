package com.newsservice.domain.model;

/**
 * Enum representing time intervals for news grouping
 * Supports various time units as specified in requirements
 */
public enum TimeInterval {
    MINUTES("minutes"),
    HOURS("hours"), 
    DAYS("days"),
    WEEKS("weeks"),
    MONTHS("months"),
    YEARS("years");

    private final String value;

    TimeInterval(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Parse string value to TimeInterval enum
     * Case-insensitive parsing with support for singular forms
     */
    public static TimeInterval fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return HOURS; // Default as per requirements
        }
        
        String normalized = value.toLowerCase().trim();
        
        // Handle singular forms
        if (normalized.equals("minute")) normalized = "minutes";
        if (normalized.equals("hour")) normalized = "hours";
        if (normalized.equals("day")) normalized = "days";
        if (normalized.equals("week")) normalized = "weeks";
        if (normalized.equals("month")) normalized = "months";
        if (normalized.equals("year")) normalized = "years";
        
        for (TimeInterval interval : values()) {
            if (interval.value.equals(normalized)) {
                return interval;
            }
        }
        
        throw new IllegalArgumentException("Invalid time interval: " + value + 
            ". Supported values: minutes, hours, days, weeks, months, years");
    }

    @Override
    public String toString() {
        return value;
    }
}
