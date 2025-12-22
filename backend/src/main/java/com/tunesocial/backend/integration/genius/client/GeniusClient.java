package com.tunesocial.backend.integration.genius.client;

import com.tunesocial.backend.integration.genius.model.GeniusTrackApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class GeniusClient {

    @Value("${genius.api.token}")
    private String token;

    private final WebClient geniusWebClient;

    public GeniusTrackApiResponse getTrack(String id) {
        return geniusWebClient.get()
                .uri("/songs/{id}", id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(GeniusTrackApiResponse.class)
                .block();
    }
}

