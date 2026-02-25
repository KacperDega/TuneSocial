package com.tunesocial.backend.integration.genius.provider;

import com.tunesocial.backend.integration.genius.adapter.GeniusAlbumAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusArtistAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusTrackAdapter;
import com.tunesocial.backend.integration.genius.client.GeniusClient;
import com.tunesocial.backend.integration.genius.exception.GeniusClientException;
import com.tunesocial.backend.integration.genius.exception.GeniusNotFoundException;
import com.tunesocial.backend.integration.genius.exception.GeniusRateLimitException;
import com.tunesocial.backend.integration.genius.exception.GeniusServerException;
import com.tunesocial.backend.integration.genius.model.GeniusResponses.*;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.exception.ExternalServiceClientException;
import com.tunesocial.backend.music.exception.ExternalServiceUnavailableException;
import com.tunesocial.backend.music.exception.MusicItemNotFoundException;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeniusMusicDataProvider implements MusicDataProvider {

    private final GeniusClient geniusClient;
    private final GeniusArtistAdapter artistAdapter;
    private final GeniusTrackAdapter trackAdapter;
    private final GeniusAlbumAdapter albumAdapter;

    private final long MAX_RETRY_WAIT_MS = 17_000;

    @Override
    @CircuitBreaker(name = "geniusApi")
    public ArtistResponse getArtist(String artistId) {
        GeniusArtistApiResponse response = callGenius(() -> geniusClient.getArtist(artistId));
        return artistAdapter.adaptArtist(response);
    }

    @Override
    @CircuitBreaker(name = "geniusApi")
    public List<AlbumSummaryResponse> getDiscography(String artistId) {
        GeniusDiscographyApiResponse response = callGenius(() -> geniusClient.getDiscography(artistId));
        return artistAdapter.adaptDiscography(response);
    }

    @Override
    @CircuitBreaker(name = "geniusApi")
    public TrackResponse getTrack(String trackId) {
        GeniusTrackApiResponse response = callGenius(() -> geniusClient.getTrack(trackId));
        return trackAdapter.adaptTrack(response);
    }

    @Override
    @CircuitBreaker(name = "geniusApi")
    public AlbumSummaryResponse getAlbum(String albumId) {
        GeniusAlbumApiResponse response = callGenius(() -> geniusClient.getAlbum(albumId));
        return albumAdapter.adaptAlbum(response);
    }

    @Override
    @CircuitBreaker(name = "geniusApi")
    public List<TrackResponse> getTrackList(String albumId) {
        GeniusTracklistApiResponse response = callGenius(() -> geniusClient.getAlbumTracklist(albumId));
        return trackAdapter.adaptTracks(response);
    }

    @Override
    @CircuitBreaker(name = "geniusApi")
    public List<TrackResponse> searchTracks(String query) {
        GeniusSearchApiResponse response = callGenius(() -> geniusClient.searchGenius(query));
        return trackAdapter.adaptTracks(response);
    }


    private <T> T callGenius(Supplier<T> supplier) {
        return executeWithRetry(supplier, 1);
    }

    private <T> T executeWithRetry(Supplier<T> supplier, int attempt) {
        try {
            return supplier.get();
        } catch (GeniusRateLimitException e) {
            long waitTime = calculateWaitTime(e, attempt);

            if (waitTime <= MAX_RETRY_WAIT_MS && attempt <= 3) {
                log.warn("Rate limit hit. Attempt {}/3. Waiting {}ms", attempt, waitTime);
                sleep(waitTime);
                return executeWithRetry(supplier, attempt + 1);
            }

            log.error("Rate limit wait time ({}ms) too long or max attempts reached.", waitTime);
            throw new ExternalServiceUnavailableException("GENIUS", e);

        } catch (GeniusServerException e) {
            long waitTime = calculateWaitTime(null, attempt);

            if (attempt <= 3) {
                log.warn("Genius service unavailable. Attempt {}/3. Waiting {}ms", attempt, waitTime);
                sleep(waitTime);
                return executeWithRetry(supplier, attempt + 1);
            }

            log.error("Genius service unavailable, max attempts reached - throwing exception.");

            throw new ExternalServiceUnavailableException("GENIUS", e);
        } catch (GeniusNotFoundException e) {
            throw new MusicItemNotFoundException("Music item not found in GENIUS", e);
        } catch (GeniusClientException e) {
            throw new ExternalServiceClientException("GENIUS", e);
        }
    }

    private long calculateWaitTime(GeniusRateLimitException e, int attempt) {
        // retryAfter present
        if (e != null && e.getRetryAfterMillis() != null) {
            return e.getRetryAfterMillis();
        }

        // retryAfter absent
        long baseWait = (long) Math.pow(2, attempt) * 1000;

        long jitter = (long) (baseWait * (Math.random() * 0.4 - 0.2));

        return baseWait + jitter;
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

}

