package com.tunesocial.backend.music.provider;

import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;

import java.util.List;

public interface MusicDataProvider {

    ArtistResponse getArtist(String artistId);

    List<AlbumSummaryResponse> getDiscography(String artistId);

    TrackResponse getTrack(String trackId);

    AlbumSummaryResponse getAlbum(String albumId);

    List<TrackResponse> getTrackList(String albumId);

    List<TrackResponse> searchTracks(String query);
}

