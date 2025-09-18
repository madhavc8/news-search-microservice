package com.newsservice.presentation.controller;

import com.newsservice.domain.model.NewsSearchRequest;
import com.newsservice.domain.model.NewsSearchResponse;
import com.newsservice.domain.model.TimeInterval;
import com.newsservice.domain.service.NewsSearchService;
import com.newsservice.domain.service.OfflineModeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for News Search operations
 * Implements HATEOAS principles and comprehensive error handling
 */
@RestController
@RequestMapping("/news")
@Validated
@Tag(name = "News Search", description = "API for searching and grouping news articles")
public class NewsSearchController {

    private final NewsSearchService newsSearchService;
    private final OfflineModeService offlineModeService;

    public NewsSearchController(NewsSearchService newsSearchService, 
                               OfflineModeService offlineModeService) {
        this.newsSearchService = newsSearchService;
        this.offlineModeService = offlineModeService;
    }

    @Operation(
        summary = "Search news articles",
        description = "Search for news articles by keyword and group them by specified time intervals"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "News articles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = NewsSearchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public Mono<ResponseEntity<EntityModel<NewsSearchResponse>>> searchNews(
            @Parameter(description = "Search keyword", required = true, example = "apple")
            @RequestParam @NotBlank(message = "Keyword is required") String keyword,
            
            @Parameter(description = "Interval value (default: 12)", example = "12")
            @RequestParam(required = false) @Positive(message = "Interval value must be positive") Integer intervalValue,
            
            @Parameter(description = "Interval unit (default: hours)", example = "hours")
            @RequestParam(required = false) String intervalUnit,
            
            @Parameter(description = "Enable offline mode", example = "false")
            @RequestParam(required = false) Boolean offlineMode) {

        NewsSearchRequest request = NewsSearchRequest.builder()
                .keyword(keyword)
                .intervalValue(intervalValue)
                .intervalUnit(intervalUnit != null ? TimeInterval.fromString(intervalUnit) : null)
                .offlineMode(offlineMode)
                .build();

        return newsSearchService.searchNews(request)
                .map(response -> {
                    EntityModel<NewsSearchResponse> entityModel = EntityModel.of(response);
                    
                    // Add HATEOAS links
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .searchNews(keyword, intervalValue, intervalUnit, offlineMode)).withSelfRel());
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .getServiceHealth()).withRel("health"));
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .getCacheStatistics()).withRel("cache-stats"));
                    
                    return ResponseEntity.ok(entityModel);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(
        summary = "Search news with POST request",
        description = "Search for news articles using a POST request with JSON body"
    )
    @PostMapping("/search")
    public Mono<ResponseEntity<EntityModel<NewsSearchResponse>>> searchNewsPost(
            @Valid @RequestBody NewsSearchRequest request) {
        
        return newsSearchService.searchNews(request)
                .map(response -> {
                    EntityModel<NewsSearchResponse> entityModel = EntityModel.of(response);
                    
                    // Add HATEOAS links
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .searchNewsPost(request)).withSelfRel());
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .getServiceHealth()).withRel("health"));
                    
                    return ResponseEntity.ok(entityModel);
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(
        summary = "Get service health",
        description = "Check the health status of the news search service and external dependencies"
    )
    @GetMapping("/health")
    public Mono<ResponseEntity<EntityModel<Map<String, Object>>>> getServiceHealth() {
        return newsSearchService.getServiceHealth()
                .map(health -> {
                    EntityModel<Map<String, Object>> entityModel = EntityModel.of(health);
                    
                    // Add HATEOAS links
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .getServiceHealth()).withSelfRel());
                    entityModel.add(linkTo(methodOn(NewsSearchController.class)
                            .searchNews("test", null, null, null)).withRel("search"));
                    
                    return ResponseEntity.ok(entityModel);
                });
    }

    @Operation(
        summary = "Get cache statistics",
        description = "Retrieve statistics about the offline cache"
    )
    @GetMapping("/cache/stats")
    public ResponseEntity<EntityModel<Map<String, Object>>> getCacheStatistics() {
        Map<String, Object> stats = offlineModeService.getCacheStatistics();
        
        EntityModel<Map<String, Object>> entityModel = EntityModel.of(stats);
        
        // Add HATEOAS links
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .getCacheStatistics()).withSelfRel());
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .clearCache()).withRel("clear-cache"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Clear cache",
        description = "Clear all cached search results"
    )
    @DeleteMapping("/cache")
    public ResponseEntity<EntityModel<Map<String, String>>> clearCache() {
        offlineModeService.clearCache();
        
        Map<String, String> response = Map.of(
                "message", "Cache cleared successfully",
                "timestamp", java.time.LocalDateTime.now().toString()
        );
        
        EntityModel<Map<String, String>> entityModel = EntityModel.of(response);
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .clearCache()).withSelfRel());
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .getCacheStatistics()).withRel("cache-stats"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Get API information",
        description = "Retrieve information about available endpoints and supported parameters"
    )
    @GetMapping("/info")
    public ResponseEntity<EntityModel<Map<String, Object>>> getApiInfo() {
        Map<String, Object> info = Map.of(
                "service", "News Search Microservice",
                "version", "1.0.0",
                "description", "Production-ready microservice for searching and grouping news articles",
                "supportedIntervals", TimeInterval.values(),
                "defaultInterval", Map.of("value", 12, "unit", "hours"),
                "features", java.util.List.of(
                        "NewsAPI integration",
                        "Date-based grouping",
                        "Offline mode support",
                        "Caching",
                        "HATEOAS compliance"
                )
        );
        
        EntityModel<Map<String, Object>> entityModel = EntityModel.of(info);
        
        // Add HATEOAS links to all available endpoints
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .getApiInfo()).withSelfRel());
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .searchNews("example", null, null, null)).withRel("search"));
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .getServiceHealth()).withRel("health"));
        entityModel.add(linkTo(methodOn(NewsSearchController.class)
                .getCacheStatistics()).withRel("cache-stats"));
        
        return ResponseEntity.ok(entityModel);
    }
}
