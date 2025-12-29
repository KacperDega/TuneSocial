package com.tunesocial.backend.integration.genius.provider;

import com.tunesocial.backend.integration.genius.adapter.GeniusArtistAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusTrackAdapter;
import com.tunesocial.backend.integration.genius.client.GeniusClient;
import com.tunesocial.backend.integration.genius.model.GeniusArtistApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusTrackApiResponse;
import com.tunesocial.backend.music.dto.AlbumDetailsResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeniusMusicDataProvider implements MusicDataProvider {

    private final GeniusClient geniusClient;
    private final GeniusArtistAdapter artistAdapter;
    private final GeniusTrackAdapter trackAdapter;

    @Override
    public ArtistResponse getArtist(String id) {
        GeniusArtistApiResponse response = geniusClient.getArtist(id);
        return artistAdapter.adapt(response);
    }

    @Override
    public TrackResponse getTrack(String id) {
        GeniusTrackApiResponse response = geniusClient.getTrack(id);
        return trackAdapter.adapt(response);
    }

    @Override
    public AlbumDetailsResponse getAlbum(String id) {
        return null;
    }
}

