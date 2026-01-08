package com.tunesocial.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient geniusApiClient() {
        return WebClient.builder()
                .baseUrl("https://api.genius.com")
                .build();
    }

    @Bean
    public WebClient geniusWebApiClient() {
        return WebClient.builder()
                .baseUrl("https://genius.com/api")
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .build();
    }
}
