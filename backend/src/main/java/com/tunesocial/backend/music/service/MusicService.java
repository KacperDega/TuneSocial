package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.AlbumDetailsResponse;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicDataProvider provider;

    public TrackResponse getTrack(String trackId) {
        return provider.getTrack(trackId);
    }

    public AlbumSummaryResponse getAlbum(String albumId) {
        return provider.getAlbum(albumId);
    }

    public ArtistResponse getArtist(String artistId) {
        return provider.getArtist(artistId);
    }

    public List<TrackResponse> getTracklist(String albumId) {
        return provider.getTrackList(albumId);
    }
}
