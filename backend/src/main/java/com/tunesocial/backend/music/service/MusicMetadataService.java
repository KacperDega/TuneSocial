package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.repository.AlbumRepository;
import com.tunesocial.backend.music.repository.ArtistRepository;
import com.tunesocial.backend.music.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MusicMetadataService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final MusicCacheService musicCacheService;
    private final MusicFetchService musicFetchService;
    private final ArtistRepository artistRepository;

    @Value("${app.cache.ttl-days}")
    private int CACHE_TTL_DAYS;

    @Value("${app.cache.expired-days}")
    private int CACHE_EXPIRED_DAYS;

    public TrackEntity getOrFetchTrack(String trackId) {
        Optional<TrackEntity> cachedTrack = trackRepository.findById(trackId);

        if (cachedTrack.isPresent()) {
            TrackEntity track = cachedTrack.get();

            if (track.isFresh(CACHE_TTL_DAYS)) {
                return track;
            }

            if (track.isFresh(CACHE_EXPIRED_DAYS)) {
                musicFetchService.refreshTrackInBackground(trackId); //async
                return track;
            }
        }

        TrackResponse response = musicFetchService.fetchTrack(trackId);
        return musicCacheService.cacheTrack(response);
    }

    public AlbumEntity getOrFetchAlbum(String albumId) {
        Optional<AlbumEntity> cachedAlbum = albumRepository.findById(albumId);

        if (cachedAlbum.isPresent()) {
            AlbumEntity album = cachedAlbum.get();

            if (album.isFresh(CACHE_TTL_DAYS)) {
                return album;
            }

            if (album.isFresh(CACHE_EXPIRED_DAYS)) {
                musicFetchService.refreshAlbumInBackground(albumId);
                return album;
            }
        }

        AlbumSummaryResponse albumResp = musicFetchService.fetchAlbum(albumId);
        List<TrackResponse> trackList = musicFetchService.fetchTracklist(albumId);
        return musicCacheService.cacheAlbumWithTracks(albumResp, trackList);
    }

    public ArtistEntity getOrFetchArtist(String artistId) {
        Optional<ArtistEntity> cachedArtist = artistRepository.findById(artistId);

        if (cachedArtist.isPresent()) {
            ArtistEntity artist = cachedArtist.get();

            if (artist.isFresh(CACHE_TTL_DAYS)) {
                return artist;
            }

            if (artist.isFresh(CACHE_EXPIRED_DAYS)) {
                musicFetchService.refreshArtistInBackground(artistId);
                return artist;
            }
        }

        ArtistResponse response = musicFetchService.fetchArtist(artistId);
        return musicCacheService.cacheArtist(response);
    }
}