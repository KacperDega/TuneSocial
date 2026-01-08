package com.tunesocial.backend.integration.genius.provider;

import com.tunesocial.backend.integration.genius.adapter.GeniusAlbumAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusArtistAdapter;
import com.tunesocial.backend.integration.genius.adapter.GeniusTrackAdapter;
import com.tunesocial.backend.integration.genius.client.GeniusClient;
import com.tunesocial.backend.integration.genius.model.GeniusAlbumApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusArtistApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusTrackApiResponse;
import com.tunesocial.backend.integration.genius.model.GeniusTracklistApiResponse;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GeniusMusicDataProvider implements MusicDataProvider {

    private final GeniusClient geniusClient;
    private final GeniusArtistAdapter artistAdapter;
    private final GeniusTrackAdapter trackAdapter;
    private final GeniusAlbumAdapter albumAdapter;

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
    public AlbumSummaryResponse getAlbum(String id) {
        GeniusAlbumApiResponse response = geniusClient.getAlbum(id);
        return albumAdapter.adapt(response);
    }

    @Override
    public List<TrackResponse> getTrackList(String albumId) {
        GeniusTracklistApiResponse response = geniusClient.getAlbumTracklist(albumId);
        return trackAdapter.adapt(response);
    }
}

