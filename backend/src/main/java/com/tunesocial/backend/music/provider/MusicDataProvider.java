package com.tunesocial.backend.music.provider;

import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;

import java.util.List;

public interface MusicDataProvider {

    ArtistResponse getArtist(String id);

    TrackResponse getTrack(String id);

    AlbumSummaryResponse getAlbum(String id);

    List<TrackResponse> getTrackList(String albumId);
}

