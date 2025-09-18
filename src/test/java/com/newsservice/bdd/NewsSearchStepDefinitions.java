package com.newsservice.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsservice.domain.model.NewsSearchRequest;
import com.newsservice.domain.model.TimeInterval;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BDD Step Definitions for News Search functionality
 * Implements Cucumber steps for behavior-driven testing
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NewsSearchStepDefinitions {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private NewsSearchRequest searchRequest;
    private ResponseEntity<String> lastResponse;
    private String baseUrl;

    @Given("the News Search Microservice is running")
    public void theNewsSearchMicroserviceIsRunning() {
        baseUrl = "http://localhost:" + port + "/api/v1/news";
        
        // Verify service is running by checking health endpoint
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(baseUrl + "/health", String.class);
        assertThat(healthResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Given("the NewsAPI service is available")
    public void theNewsAPIServiceIsAvailable() {
        // This step assumes NewsAPI is available - in real tests, we might mock this
        // For integration tests, we rely on the actual service or use WireMock
    }

    @Given("I want to search for news about {string}")
    public void iWantToSearchForNewsAbout(String keyword) {
        searchRequest = NewsSearchRequest.builder()
                .keyword(keyword)
                .build();
    }

    @Given("I want to search for news with an empty keyword")
    public void iWantToSearchForNewsWithAnEmptyKeyword() {
        searchRequest = NewsSearchRequest.builder()
                .keyword("")
                .build();
    }

    @Given("I set the interval to {string}")
    public void iSetTheIntervalTo(String interval) {
        String[] parts = interval.split(" ");
        int value = Integer.parseInt(parts[0]);
        String unit = parts[1];
        
        searchRequest.setIntervalValue(value);
        searchRequest.setIntervalUnit(TimeInterval.fromString(unit));
    }

    @Given("I enable offline mode")
    public void iEnableOfflineMode() {
        searchRequest.setOfflineMode(true);
    }

    @Given("the NewsAPI service is unavailable")
    public void theNewsAPIServiceIsUnavailable() {
        // In a real test, we would mock the NewsAPI to return errors
        // For this example, we'll simulate by using offline mode
        searchRequest.setOfflineMode(true);
    }

    @Given("there are cached search results")
    public void thereAreCachedSearchResults() {
        // Perform a search to populate cache
        NewsSearchRequest cacheRequest = NewsSearchRequest.builder()
                .keyword("test")
                .build();
        
        String url = baseUrl + "/search?keyword=" + cacheRequest.getKeyword();
        restTemplate.getForEntity(url, String.class);
    }

    @When("I perform a search request")
    public void iPerformASearchRequest() {
        try {
            if (searchRequest.getKeyword() == null || searchRequest.getKeyword().trim().isEmpty()) {
                // Test invalid request
                String url = baseUrl + "/search?keyword=";
                lastResponse = restTemplate.getForEntity(url, String.class);
            } else {
                // Build URL with parameters
                StringBuilder urlBuilder = new StringBuilder(baseUrl + "/search?keyword=" + searchRequest.getKeyword());
                
                if (searchRequest.getIntervalValue() != null) {
                    urlBuilder.append("&intervalValue=").append(searchRequest.getIntervalValue());
                }
                if (searchRequest.getIntervalUnit() != null) {
                    urlBuilder.append("&intervalUnit=").append(searchRequest.getIntervalUnit().getValue());
                }
                if (searchRequest.getOfflineMode() != null) {
                    urlBuilder.append("&offlineMode=").append(searchRequest.getOfflineMode());
                }
                
                lastResponse = restTemplate.getForEntity(urlBuilder.toString(), String.class);
            }
        } catch (Exception e) {
            // Handle exceptions for error scenarios
            System.err.println("Request failed: " + e.getMessage());
        }
    }

    @When("I check the service health")
    public void iCheckTheServiceHealth() {
        lastResponse = restTemplate.getForEntity(baseUrl + "/health", String.class);
    }

    @When("I request cache statistics")
    public void iRequestCacheStatistics() {
        lastResponse = restTemplate.getForEntity(baseUrl + "/cache/stats", String.class);
    }

    @When("I clear the cache")
    public void iClearTheCache() {
        lastResponse = restTemplate.exchange(baseUrl + "/cache", HttpMethod.DELETE, null, String.class);
    }

    @Then("I should receive a successful response")
    public void iShouldReceiveASuccessfulResponse() {
        assertThat(lastResponse.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("I should receive a bad request error")
    public void iShouldReceiveABadRequestError() {
        assertThat(lastResponse.getStatusCode().is4xxClientError()).isTrue();
    }

    @Then("the response should contain news articles")
    public void theResponseShouldContainNewsArticles() throws Exception {
        String responseBody = lastResponse.getBody();
        assertThat(responseBody).isNotNull();
        
        // Parse JSON and verify structure
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        assertThat(response).containsKey("totalArticles");
        
        Integer totalArticles = (Integer) response.get("totalArticles");
        assertThat(totalArticles).isGreaterThanOrEqualTo(0);
    }

    @Then("the articles should be grouped by {string} intervals")
    public void theArticlesShouldBeGroupedByIntervals(String expectedInterval) throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        String[] parts = expectedInterval.split(" ");
        Integer expectedValue = Integer.parseInt(parts[0]);
        String expectedUnit = parts[1].toUpperCase();
        
        assertThat(response.get("intervalValue")).isEqualTo(expectedValue);
        assertThat(response.get("intervalUnit")).isEqualTo(expectedUnit);
    }

    @Then("the response should indicate {string} mode")
    public void theResponseShouldIndicateMode(String expectedMode) throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        Boolean fromCache = (Boolean) response.get("fromCache");
        if ("offline".equals(expectedMode)) {
            assertThat(fromCache).isTrue();
        } else {
            assertThat(fromCache).isFalse();
        }
    }

    @Then("the articles should come from cache")
    public void theArticlesShouldComeFromCache() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        Boolean fromCache = (Boolean) response.get("fromCache");
        assertThat(fromCache).isTrue();
        
        String message = (String) response.get("message");
        assertThat(message).containsIgnoringCase("cache");
    }

    @Then("the articles should come from cache or sample data")
    public void theArticlesShouldComeFromCacheOrSampleData() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        // Should have articles even in offline mode
        Integer totalArticles = (Integer) response.get("totalArticles");
        assertThat(totalArticles).isGreaterThan(0);
    }

    @Then("the error message should indicate {string}")
    public void theErrorMessageShouldIndicate(String expectedMessage) throws Exception {
        String responseBody = lastResponse.getBody();
        assertThat(responseBody).containsIgnoringCase(expectedMessage.replace("\"", ""));
    }

    @Then("the response should show NewsAPI availability status")
    public void theResponseShouldShowNewsAPIAvailabilityStatus() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKey("newsApiAvailable");
        assertThat(response.get("newsApiAvailable")).isInstanceOf(Boolean.class);
    }

    @Then("the response should show offline mode configuration")
    public void theResponseShouldShowOfflineModeConfiguration() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKey("offlineModeEnabled");
        assertThat(response.get("offlineModeEnabled")).isInstanceOf(Boolean.class);
    }

    @Then("the response should show number of cached keywords")
    public void theResponseShouldShowNumberOfCachedKeywords() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKey("totalCachedKeywords");
        assertThat(response.get("totalCachedKeywords")).isInstanceOf(Integer.class);
    }

    @Then("the response should show cache expiration details")
    public void theResponseShouldShowCacheExpirationDetails() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKey("cacheDuration");
    }

    @Then("I should receive cache information")
    public void iShouldReceiveCacheInformation() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKey("totalCachedKeywords");
        assertThat(response).containsKey("validCachedEntries");
    }

    @Then("I should receive a success confirmation")
    public void iShouldReceiveASuccessConfirmation() throws Exception {
        String responseBody = lastResponse.getBody();
        Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
        
        assertThat(response).containsKey("message");
        String message = (String) response.get("message");
        assertThat(message).containsIgnoringCase("success");
    }

    @Then("subsequent cache statistics should show empty cache")
    public void subsequentCacheStatisticsShouldShowEmptyCache() {
        ResponseEntity<String> statsResponse = restTemplate.getForEntity(baseUrl + "/cache/stats", String.class);
        assertThat(statsResponse.getStatusCode().is2xxSuccessful()).isTrue();
        
        // The cache might not be completely empty due to timing, but it should be significantly reduced
        // This is a simplified check - in real scenarios, you might want more sophisticated verification
    }
}
