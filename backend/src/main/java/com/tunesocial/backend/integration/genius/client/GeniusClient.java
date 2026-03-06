package com.tunesocial.backend.integration.genius.client;

import com.tunesocial.backend.integration.genius.exception.*;
import com.tunesocial.backend.integration.genius.model.GeniusResponses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class GeniusClient {

    @Value("${genius.api.token}")
    private String token;

    private final WebClient geniusApiClient;
    private final WebClient geniusWebApiClient;


    // TODO: refactor fetch and fetchFromWebApiById methods
    private <T> T fetch(Class<T> responseType, Function<UriBuilder, URI> uriFunction, String context) {
        return execute(
                geniusApiClient.get()
                        .uri(uriFunction)
                        .header("Authorization", "Bearer " + token)
                        .retrieve()
                        .onStatus(
                                status -> status.is4xxClientError() && status.value() == 404,
                                response -> Mono.error(new GeniusNotFoundException(context))
                        )
                        .onStatus(
                                status -> status.value() == 429,
                                response -> Mono.error(
                                        new GeniusRateLimitException(
                                                parseRetryAfter(response.headers().asHttpHeaders().getFirst("Retry-After"))
                                        )
                                )
                        )
                        .onStatus(
                                HttpStatusCode::is4xxClientError,
                                r -> Mono.error(new GeniusClientException(r.statusCode().value()))
                        )
                        .onStatus(
                                HttpStatusCode::is5xxServerError,
                                response -> Mono.error(new GeniusServerException())
                        )
                        .bodyToMono(responseType)
        );
    }

    private <T> T fetchById(String id, String path, Class<T> responseType) {
        return fetch(
                responseType,
                uri -> uri.path(path).build(id),
                id
        );
    }

    public GeniusTrackApiResponse getTrack(String id) {
        return fetchById(id, "/songs/{id}", GeniusTrackApiResponse.class);
    }

    public GeniusArtistApiResponse getArtist(String id) {
        return fetchById(id, "/artists/{id}", GeniusArtistApiResponse.class);
    }

    public GeniusAlbumApiResponse getAlbum(String id) {
        return fetchById(id, "/albums/{id}", GeniusAlbumApiResponse.class);
    }

    public GeniusTracklistApiResponse getAlbumTracklist(String albumId){
        return fetchById(albumId, "/albums/{album_id}/tracks", GeniusTracklistApiResponse.class);
    }

    public GeniusSearchApiResponse searchGenius(String query) {
        return fetch(
                GeniusSearchApiResponse.class,
                uri -> uri.path("/search").queryParam("q", query).build(),
                query
        );
    }


    private <T> T fetchFromWebApiById(String id, String path, Class<T> responseType) {
        return execute(
                geniusWebApiClient.get()
                        .uri(path, id)
                        .retrieve()
                        .onStatus(
                                status -> status.value() == 404,
                                response -> Mono.error(new GeniusNotFoundException(id))
                        )
                        .onStatus(
                                status -> status.value() == 429,
                                response -> Mono.error(
                                        new GeniusRateLimitException(
                                                parseRetryAfter(response.headers().asHttpHeaders().getFirst("Retry-After"))
                                        )
                                )
                        )
                        .onStatus(
                                HttpStatusCode::is4xxClientError,
                                r -> Mono.error(new GeniusClientException(r.statusCode().value()))
                        )
                        .onStatus(
                                HttpStatusCode::is5xxServerError,
                                response -> Mono.error(new GeniusServerException())
                        )
                        .bodyToMono(responseType)
        );
    }

    public GeniusDiscographyApiResponse getDiscography(String artistId) {
        return fetchFromWebApiById(artistId, "/artists/{id}/albums", GeniusDiscographyApiResponse.class);
    }

    private <T> T execute(Mono<T> request) {
        try {
            return request.block();
        } catch (GeniusException e) {
            throw e;
        } catch (WebClientRequestException e) {
            throw new GeniusServerException(e);
        }
    }

    private Long parseRetryAfter(String header) {
        Long fallback = null;

        if (header == null || header.isBlank()) {
            return fallback;
        }

        // retryAfter is seconds
        try {
            return Long.parseLong(header);
        } catch (NumberFormatException ignored) {
        }

        // retryAfter is date
        try {
            ZonedDateTime retryTime = ZonedDateTime.parse(
                    header,
                    DateTimeFormatter.RFC_1123_DATE_TIME);

            long seconds = Duration.between(ZonedDateTime.now(ZoneOffset.UTC), retryTime).getSeconds();

            if (seconds <= 0) return fallback;
            return seconds;

        } catch (Exception ignored) {
            return fallback;
        }
    }
}
