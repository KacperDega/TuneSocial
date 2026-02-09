package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MusicFetchService {

    private final MusicDataProvider musicDataProvider;
    private final MusicCacheService musicCacheService;

    public TrackResponse fetchTrack(String trackId) {
        return musicDataProvider.getTrack(trackId);
    }

    public AlbumSummaryResponse fetchAlbum(String albumId) {
        return musicDataProvider.getAlbum(albumId);
    }

    public List<TrackResponse> fetchTracklist(String albumId) {
        return musicDataProvider.getTrackList(albumId);
    }

    @Async
    @Transactional
    public void refreshTrackInBackground(String trackId) {
        try {
            TrackResponse response = fetchTrack(trackId);
            musicCacheService.cacheTrack(response);
            log.debug("Background refresh success for track: {}", trackId);
        } catch (Exception e) {
            log.error("Background refresh failed for track: {}", trackId, e);
        }
    }
}
