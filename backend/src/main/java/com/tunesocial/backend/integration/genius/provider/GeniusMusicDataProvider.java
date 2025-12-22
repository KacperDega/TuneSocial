package com.tunesocial.backend.integration.genius.provider;

import com.tunesocial.backend.integration.genius.adapter.GeniusTrackAdapter;
import com.tunesocial.backend.integration.genius.client.GeniusClient;
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
    private final GeniusTrackAdapter trackAdapter;

    @Override
    public ArtistResponse getArtist(String id) {
        return null;
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

