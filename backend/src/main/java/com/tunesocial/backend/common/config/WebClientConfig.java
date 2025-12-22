package com.tunesocial.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient geniusWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.genius.com")
                .build();
    }
}

