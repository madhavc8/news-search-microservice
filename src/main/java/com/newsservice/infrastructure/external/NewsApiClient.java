package com.newsservice.infrastructure.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.newsservice.domain.model.NewsArticle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Client for NewsAPI.org integration
 * Implements retry logic and error handling
 */
@Component
public class NewsApiClient {

    private final WebClient webClient;
    private final String apiKey;
    private final Duration timeout;
    private final int retryAttempts;

    public NewsApiClient(WebClient.Builder webClientBuilder,
                        @Value("${news-api.base-url}") String baseUrl,
                        @Value("${news-api.api-key}") String apiKey,
                        @Value("${news-api.timeout}") Duration timeout,
                        @Value("${news-api.retry-attempts}") int retryAttempts) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.timeout = timeout;
        this.retryAttempts = retryAttempts;
    }

    /**
     * Search for news articles using the Everything API
     * 
     * @param keyword Search keyword
     * @param from Start date for search
     * @param to End date for search
     * @return List of news articles
     */
    public Mono<List<NewsArticle>> searchNews(String keyword, LocalDateTime from, LocalDateTime to) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/everything")
                        .queryParam("q", keyword)
                        .queryParam("from", from.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .queryParam("to", to.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .queryParam("sortBy", "publishedAt")
                        .queryParam("pageSize", 100)
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatus.TOO_MANY_REQUESTS::equals, 
                    response -> Mono.error(new NewsApiException("Rate limit exceeded")))
                .onStatus(HttpStatus.UNAUTHORIZED::equals,
                    response -> Mono.error(new NewsApiException("Invalid API key")))
                .onStatus(status -> status.is4xxClientError(),
                    response -> Mono.error(new NewsApiException("Client error: " + response.statusCode())))
                .onStatus(status -> status.is5xxServerError(),
                    response -> Mono.error(new NewsApiException("Server error: " + response.statusCode())))
                .bodyToMono(NewsApiResponse.class)
                .map(NewsApiResponse::getArticles)
                .timeout(timeout)
                .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1))
                        .filter(throwable -> !(throwable instanceof NewsApiException)))
                .onErrorMap(WebClientResponseException.class, 
                    ex -> new NewsApiException("Failed to fetch news: " + ex.getMessage(), ex));
    }

    /**
     * Check if the NewsAPI service is available
     */
    public Mono<Boolean> isServiceAvailable() {
        return webClient.get()
                .uri("/top-headlines?country=us&pageSize=1&apiKey=" + apiKey)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(5))
                .onErrorReturn(false);
    }

    /**
     * Response model for NewsAPI
     */
    public static class NewsApiResponse {
        private String status;
        private Integer totalResults;
        private List<NewsArticle> articles;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Integer getTotalResults() {
            return totalResults;
        }

        public void setTotalResults(Integer totalResults) {
            this.totalResults = totalResults;
        }

        public List<NewsArticle> getArticles() {
            return articles;
        }

        public void setArticles(List<NewsArticle> articles) {
            this.articles = articles;
        }
    }

    /**
     * Custom exception for NewsAPI related errors
     */
    public static class NewsApiException extends RuntimeException {
        public NewsApiException(String message) {
            super(message);
        }

        public NewsApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
