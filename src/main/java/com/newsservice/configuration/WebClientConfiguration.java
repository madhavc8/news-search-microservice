package com.newsservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Configuration for WebClient used in external API calls
 * Implements performance optimizations and connection pooling
 */
@Configuration
public class WebClientConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configure connection pool for better performance
        ConnectionProvider connectionProvider = ConnectionProvider.builder("news-api-pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(30))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(io.netty.channel.ChannelOption.SO_KEEPALIVE, true)
                .option(io.netty.channel.ChannelOption.TCP_NODELAY, true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)); // 1MB buffer
    }
}
