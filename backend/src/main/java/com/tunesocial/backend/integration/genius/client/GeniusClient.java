package com.tunesocial.backend.integration.genius.client;

import com.tunesocial.backend.integration.genius.model.*;
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

    private <T> T getById(String id, String uri, Class<T> responseType) {
        return geniusWebClient.get()
                .uri(uri, id)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public GeniusTrackApiResponse getTrack(String id) {
        return getById(id, "/songs/{id}", GeniusTrackApiResponse.class);
    }

    public GeniusArtistApiResponse getArtist(String id) {
        return getById(id, "/artists/{id}", GeniusArtistApiResponse.class);
    }

    public GeniusAlbumApiResponse getAlbum(String id) {
        return getById(id, "/albums/{id}", GeniusAlbumApiResponse.class);
    }
}

