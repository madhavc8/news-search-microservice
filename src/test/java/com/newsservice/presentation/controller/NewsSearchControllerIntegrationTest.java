package com.newsservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsservice.domain.model.NewsSearchRequest;
import com.newsservice.domain.model.TimeInterval;
import com.newsservice.domain.service.NewsSearchService;
import com.newsservice.domain.service.OfflineModeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for NewsSearchController
 * Tests REST API endpoints and request/response handling
 */
@WebMvcTest(NewsSearchController.class)
class NewsSearchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NewsSearchService newsSearchService;

    @MockBean
    private OfflineModeService offlineModeService;

    @Test
    void searchNews_WithValidParameters_ShouldReturnOk() throws Exception {
        // Given
        when(newsSearchService.searchNews(any())).thenReturn(Mono.just(createMockResponse()));

        // When & Then
        mockMvc.perform(get("/news/search")
                .param("keyword", "apple")
                .param("intervalValue", "12")
                .param("intervalUnit", "hours")
                .param("offlineMode", "false"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.keyword").value("apple"))
                .andExpect(jsonPath("$.intervalValue").value(12))
                .andExpect(jsonPath("$.intervalUnit").value("HOURS"))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void searchNews_WithMissingKeyword_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/news/search")
                .param("intervalValue", "12"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchNews_WithInvalidIntervalValue_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/news/search")
                .param("keyword", "apple")
                .param("intervalValue", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchNews_WithInvalidIntervalUnit_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/news/search")
                .param("keyword", "apple")
                .param("intervalUnit", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchNewsPost_WithValidRequest_ShouldReturnOk() throws Exception {
        // Given
        NewsSearchRequest request = NewsSearchRequest.builder()
                .keyword("technology")
                .intervalValue(6)
                .intervalUnit(TimeInterval.HOURS)
                .offlineMode(false)
                .build();

        when(newsSearchService.searchNews(any())).thenReturn(Mono.just(createMockResponse()));

        // When & Then
        mockMvc.perform(post("/news/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void searchNewsPost_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - request with empty keyword
        NewsSearchRequest request = new NewsSearchRequest();
        request.setKeyword("");
        request.setIntervalValue(12);

        // When & Then
        mockMvc.perform(post("/news/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getServiceHealth_ShouldReturnHealthStatus() throws Exception {
        // Given
        Map<String, Object> healthStatus = new HashMap<>();
        healthStatus.put("newsApiAvailable", true);
        healthStatus.put("offlineModeEnabled", false);
        healthStatus.put("status", "UP");

        when(newsSearchService.getServiceHealth()).thenReturn(Mono.just(healthStatus));

        // When & Then
        mockMvc.perform(get("/news/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.newsApiAvailable").value(true))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void getCacheStatistics_ShouldReturnCacheStats() throws Exception {
        // Given
        Map<String, Object> cacheStats = new HashMap<>();
        cacheStats.put("totalCachedKeywords", 5);
        cacheStats.put("validCachedEntries", 3L);

        when(offlineModeService.getCacheStatistics()).thenReturn(cacheStats);

        // When & Then
        mockMvc.perform(get("/news/cache/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalCachedKeywords").value(5))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.clear-cache").exists());
    }

    @Test
    void clearCache_ShouldReturnSuccessMessage() throws Exception {
        mockMvc.perform(delete("/news/cache"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Cache cleared successfully"))
                .andExpect(jsonPath("$._links").exists());
    }

    @Test
    void getApiInfo_ShouldReturnApiInformation() throws Exception {
        mockMvc.perform(get("/news/info"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.service").value("News Search Microservice"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.supportedIntervals").isArray())
                .andExpect(jsonPath("$.features").isArray())
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.search").exists())
                .andExpect(jsonPath("$._links.health").exists());
    }

    private com.newsservice.domain.model.NewsSearchResponse createMockResponse() {
        com.newsservice.domain.model.NewsSearchResponse response = 
                new com.newsservice.domain.model.NewsSearchResponse("apple", 12, TimeInterval.HOURS);
        response.setTotalArticles(10);
        response.setFromCache(false);
        response.setMessage("Results fetched from NewsAPI");
        return response;
    }
}
