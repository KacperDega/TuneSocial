package com.tunesocial.backend.integration.genius.provider;

import com.tunesocial.backend.integration.genius.adapter.GeniusAlbumAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusArtistAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusTrackAdapter;
import com.tunesocial.backend.integration.genius.client.GeniusClient;
import com.tunesocial.backend.integration.genius.exception.GeniusClientException;
import com.tunesocial.backend.integration.genius.exception.GeniusNotFoundException;
import com.tunesocial.backend.integration.genius.exception.GeniusServerException;
import com.tunesocial.backend.integration.genius.model.GeniusAlbumApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusArtistApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusTrackApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusTracklistApiResponse;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.exception.ExternalServiceClientException;
import com.tunesocial.backend.music.exception.ExternalServiceUnavailableException;
import com.tunesocial.backend.music.exception.MusicItemNotFoundException;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class GeniusMusicDataProvider implements MusicDataProvider {

    private final GeniusClient geniusClient;
    private final GeniusArtistAdapter artistAdapter;
    private final GeniusTrackAdapter trackAdapter;
    private final GeniusAlbumAdapter albumAdapter;

    private <T> T callGenius(Supplier<T> supplier) {
        try {
            return supplier.get();

        } catch (GeniusNotFoundException e) {
            throw new MusicItemNotFoundException("Music item not found in GENIUS", e);

        } catch (GeniusServerException e) {
            throw new ExternalServiceUnavailableException("GENIUS", e);

        } catch (GeniusClientException e) {
            throw new ExternalServiceClientException("GENIUS", e);
        }
    }


    @Override
    public ArtistResponse getArtist(String id) {
        GeniusArtistApiResponse response = callGenius(() -> geniusClient.getArtist(id));
        return artistAdapter.adapt(response);
    }

    @Override
    public TrackResponse getTrack(String id) {
        GeniusTrackApiResponse response = callGenius(() -> geniusClient.getTrack(id));
        return trackAdapter.adapt(response);
    }

    @Override
    public AlbumSummaryResponse getAlbum(String id) {
        GeniusAlbumApiResponse response = callGenius(() -> geniusClient.getAlbum(id));
        return albumAdapter.adapt(response);
    }

    @Override
    public List<TrackResponse> getTrackList(String albumId) {
        GeniusTracklistApiResponse response = callGenius(() -> geniusClient.getAlbumTracklist(albumId));
        return trackAdapter.adapt(response);
    }

}

