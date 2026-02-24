package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.repository.AlbumRepository;
import com.tunesocial.backend.music.repository.ArtistRepository;
import com.tunesocial.backend.music.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MusicMetadataService {

    private final TrackRepository trackRepository;
    private final AlbumRepository albumRepository;
    private final MusicCacheService musicCacheService;
    private final MusicFetchService musicFetchService;
    private final ArtistRepository artistRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    @Value("${app.cache.ttl-days:30}")
    private int CACHE_TTL_DAYS;

    @Value("${app.cache.expired-days:60}")
    private int CACHE_EXPIRED_DAYS;

    @Value("${app.cache.discography-ttl-days:7}")
    private int DISCOGRAPHY_CACHE_TTL_DAYS;

    @Value("${app.cache.discography-expired-days:14}")
    private int DISCOGRAPHY_CACHE_EXPIRED_DAYS;


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

    public Map<String, TrackEntity> getOrFetchTracks(List<String> trackIds) {
        List<TrackEntity> existingTracks = trackRepository.findAllById(trackIds);

        Map<String, TrackEntity> trackMap = existingTracks.stream()
                .collect(Collectors.toMap(TrackEntity::getId, t -> t));

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String id : trackIds) {
            TrackEntity track = trackMap.get(id);

            if (track == null || !track.isFresh(CACHE_EXPIRED_DAYS)) {
                // expired - refresh then return
                futures.add(CompletableFuture.runAsync(() -> {
                    log.info("Fetching fresh data for track: {}", id);
                    TrackResponse resp = musicFetchService.fetchTrack(id);
                    TrackEntity cached = musicCacheService.cacheTrack(resp);

                    synchronized (trackMap) {
                        trackMap.put(id, cached);
                    }
                }, executor));
            } else if (!track.isFresh(CACHE_TTL_DAYS)) {
                // stale - return + async refresh
                musicFetchService.refreshTrackInBackground(id);
            }
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        return trackMap;
    }

    public Map<String, AlbumEntity> getOrFetchAlbums(List<String> albumIds) {
        List<AlbumEntity> existingAlbums = albumRepository.findAllById(albumIds);

        Map<String, AlbumEntity> albumMap = existingAlbums.stream()
                .collect(Collectors.toMap(AlbumEntity::getId, a -> a));

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String id : albumIds) {
            AlbumEntity album = albumMap.get(id);

            if (album == null || !album.isFresh(CACHE_EXPIRED_DAYS)) {
                // expired - refresh then return
                futures.add(CompletableFuture.runAsync(() -> {
                    log.info("Fetching fresh data for album: {}", id);

                    AlbumSummaryResponse albumResp = musicFetchService.fetchAlbum(id);
                    List<TrackResponse> trackList = musicFetchService.fetchTracklist(id);

                    AlbumEntity cached = musicCacheService.cacheAlbumWithTracks(albumResp, trackList);

                    synchronized (albumMap) {
                        albumMap.put(id, cached);
                    }
                }, executor));
            } else if (!album.isFresh(CACHE_TTL_DAYS)) {
                // stale - return + async refresh
                musicFetchService.refreshAlbumInBackground(id);
            }
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        return albumMap;
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

    public List<AlbumEntity> getOrFetchDiscography(String artistId) {
        ArtistEntity artist = getOrFetchArtist(artistId);

        // FRESH (< 7 days)
        if (artist.isDiscographyFresh(DISCOGRAPHY_CACHE_TTL_DAYS)) {
            return albumRepository.findAllByArtists_Id(artistId);
        }

        // STALE (7–14 days)
        if (artist.isDiscographyFresh(DISCOGRAPHY_CACHE_EXPIRED_DAYS)) {
            musicFetchService.refreshDiscographyInBackground(artistId);
            return albumRepository.findAllByArtists_Id(artistId);
        }

        // EXPIRED or no artist
        List<AlbumSummaryResponse> discography = musicFetchService.fetchDiscography(artistId);

        return musicCacheService.cacheDiscography(artistId, discography);
    }
}