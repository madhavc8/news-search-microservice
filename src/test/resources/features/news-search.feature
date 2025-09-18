Feature: News Search Functionality
  As a user of the News Search Microservice
  I want to search for news articles by keyword
  So that I can get relevant news grouped by time intervals

  Background:
    Given the News Search Microservice is running
    And the NewsAPI service is available

  Scenario: Search for news with default parameters
    Given I want to search for news about "apple"
    When I perform a search request
    Then I should receive a successful response
    And the response should contain news articles
    And the articles should be grouped by "12 hours" intervals
    And the response should indicate "online" mode

  Scenario: Search for news with custom time interval
    Given I want to search for news about "technology"
    And I set the interval to "6 hours"
    When I perform a search request
    Then I should receive a successful response
    And the articles should be grouped by "6 hours" intervals

  Scenario: Search for news with different time units
    Given I want to search for news about "sports"
    And I set the interval to "2 days"
    When I perform a search request
    Then I should receive a successful response
    And the articles should be grouped by "2 days" intervals

  Scenario: Search for news in offline mode
    Given I want to search for news about "bitcoin"
    And I enable offline mode
    When I perform a search request
    Then I should receive a successful response
    And the response should indicate "offline" mode
    And the articles should come from cache

  Scenario: Search with invalid keyword
    Given I want to search for news with an empty keyword
    When I perform a search request
    Then I should receive a bad request error
    And the error message should indicate "keyword is required"

  Scenario: Search with invalid interval value
    Given I want to search for news about "finance"
    And I set the interval to "-5 hours"
    When I perform a search request
    Then I should receive a bad request error
    And the error message should indicate "interval value must be positive"

  Scenario: Fallback to offline mode when API is unavailable
    Given I want to search for news about "weather"
    And the NewsAPI service is unavailable
    When I perform a search request
    Then I should receive a successful response
    And the response should indicate "offline" mode
    And the articles should come from cache or sample data

  Scenario: Health check shows service status
    When I check the service health
    Then I should receive a successful response
    And the response should show NewsAPI availability status
    And the response should show offline mode configuration

  Scenario: Cache statistics are available
    Given there are cached search results
    When I request cache statistics
    Then I should receive cache information
    And the response should show number of cached keywords
    And the response should show cache expiration details

  Scenario: Clear cache functionality
    Given there are cached search results
    When I clear the cache
    Then I should receive a success confirmation
    And subsequent cache statistics should show empty cache
