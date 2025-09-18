package com.newsservice.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for comprehensive API documentation
 * Provides detailed API specifications with examples and error codes
 */
@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI newsSearchOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("News Search Microservice API")
                        .description("""
                                Production-ready microservice for searching and grouping news articles.
                                
                                ## Features
                                - Search news articles by keyword using NewsAPI.org
                                - Group results by customizable time intervals (minutes, hours, days, weeks, months, years)
                                - Offline mode support with intelligent caching
                                - HATEOAS compliant REST API
                                - Comprehensive error handling
                                - Performance optimized with connection pooling
                                - Security headers and best practices
                                
                                ## Default Behavior
                                - Default interval: 12 hours if not specified
                                - Automatic fallback to offline mode when NewsAPI is unavailable
                                - Intelligent caching for improved performance
                                
                                ## Error Codes
                                - **400**: Bad Request - Invalid parameters or validation errors
                                - **404**: Not Found - Endpoint not found
                                - **429**: Too Many Requests - Rate limit exceeded
                                - **500**: Internal Server Error - Unexpected server error
                                - **503**: Service Unavailable - External API unavailable
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("News Service Team")
                                .email("support@newsservice.com")
                                .url("https://github.com/newsservice/news-search-microservice"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/v1")
                                .description("Development server"),
                        new Server()
                                .url("https://api.newsservice.com/api/v1")
                                .description("Production server")))
                .components(new Components()
                        .addSecuritySchemes("ApiKeyAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-API-Key")
                                .description("API Key for accessing protected endpoints")));
    }
}
