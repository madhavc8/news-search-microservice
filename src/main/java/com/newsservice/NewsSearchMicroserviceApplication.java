package com.newsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for News Search Microservice
 * 
 * This microservice provides news search functionality with:
 * - NewsAPI integration
 * - Date-based grouping
 * - Offline mode support
 * - Caching capabilities
 * - Production-ready features
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class NewsSearchMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsSearchMicroserviceApplication.class, args);
    }
}
