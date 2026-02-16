package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.repository.AlbumRepository;
import com.tunesocial.backend.music.repository.ArtistRepository;
import com.tunesocial.backend.music.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicMetadataServiceTest {

    @Mock private TrackRepository trackRepository;
    @Mock private MusicFetchService musicFetchService;
    @Mock private MusicCacheService musicCacheService;
    @Mock private AlbumRepository albumRepository;
    @Mock private ArtistRepository artistRepository;

    @InjectMocks private MusicMetadataService musicMetadataService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(musicMetadataService, "CACHE_TTL_DAYS", 30);
        ReflectionTestUtils.setField(musicMetadataService, "CACHE_EXPIRED_DAYS", 60);
        ReflectionTestUtils.setField(musicMetadataService, "DISCOGRAPHY_CACHE_TTL_DAYS", 7);
        ReflectionTestUtils.setField(musicMetadataService, "DISCOGRAPHY_CACHE_EXPIRED_DAYS", 14);
    }

    @Nested
    class getOrFetchTrack {

        @Test
        @DisplayName("When track is fresh (track < ttl) - return it and do nothing else")
        void shouldReturnCachedTrackWhenFresh() {
            // Given
            String id = "t1";
            TrackEntity freshTrack = createTrackWithDate(id, LocalDateTime.now().minusDays(5));
            when(trackRepository.findById(id)).thenReturn(Optional.of(freshTrack));

            // When
            TrackEntity result = musicMetadataService.getOrFetchTrack(id);

            // Then
            assertThat(result).isEqualTo(freshTrack);
            verifyNoInteractions(musicFetchService, musicCacheService);
        }

        @Test
        @DisplayName("When track is stale (ttl < track < expired) - return it and refresh in background")
        void shouldReturnCachedTrackAndTriggerBackgroundRefreshWhenStale() {
            // Given
            String id = "t1";
            TrackEntity staleTrack = createTrackWithDate(id, LocalDateTime.now().minusDays(45));
            when(trackRepository.findById(id)).thenReturn(Optional.of(staleTrack));

            // When
            TrackEntity result = musicMetadataService.getOrFetchTrack(id);

            // Then
            assertThat(result).isEqualTo(staleTrack);
            verify(musicFetchService).refreshTrackInBackground(id);
            verify(musicFetchService, never()).fetchTrack(anyString());
        }

        @Test
        @DisplayName("When track is expired (expired < track) - refresh it and then return")
        void shouldFetchAndCacheWhenTrackExpired() {
            // Given
            String id = "t1";
            TrackEntity expiredTrack = createTrackWithDate(id, LocalDateTime.now().minusDays(70));
            TrackResponse response = new TrackResponse(id, "Title", null, null, "2024", List.of(), List.of());

            when(trackRepository.findById(id)).thenReturn(Optional.of(expiredTrack));
            when(musicFetchService.fetchTrack(id)).thenReturn(response);
            when(musicCacheService.cacheTrack(response)).thenReturn(new TrackEntity());

            // When
            musicMetadataService.getOrFetchTrack(id);

            // Then
            verify(musicFetchService).fetchTrack(id);
            verify(musicCacheService).cacheTrack(response);
        }

    }


    @Nested
    class GetOrFetchAlbum {
        @Test
        @DisplayName("Should return fresh album from cache")
        void shouldReturnFreshAlbum() {

            // Given
            String id = "alb1";
            AlbumEntity freshAlbum = createAlbum(id, LocalDateTime.now().minusDays(5));
            when(albumRepository.findById(id)).thenReturn(Optional.of(freshAlbum));

            // When
            AlbumEntity result = musicMetadataService.getOrFetchAlbum(id);

            // Then
            assertThat(result).isEqualTo(freshAlbum);
            verifyNoInteractions(musicFetchService, musicCacheService);
        }

        @Test
        @DisplayName("Should fetch and cache when album is missing")
        void shouldFetchAndCacheNewAlbum() {

            // Given
            String id = "alb1";
            AlbumSummaryResponse resp = new AlbumSummaryResponse(id, "Title", List.of(), "url", "2024");
            List<TrackResponse> tracks = List.of();

            when(albumRepository.findById(id)).thenReturn(Optional.empty());
            when(musicFetchService.fetchAlbum(id)).thenReturn(resp);
            when(musicFetchService.fetchTracklist(id)).thenReturn(tracks);

            // When
            musicMetadataService.getOrFetchAlbum(id);

            // Then
            verify(musicCacheService).cacheAlbumWithTracks(resp, tracks);
        }
    }

    @Nested
    class GetOrFetchArtist {

        @Test
        @DisplayName("Should return stale artist and trigger async refresh")
        void shouldReturnStaleArtist() {

            // Given
            String id = "art1";
            ArtistEntity staleArtist = createArtist(id, LocalDateTime.now().minusDays(45));
            when(artistRepository.findById(id)).thenReturn(Optional.of(staleArtist));

            // When
            ArtistEntity result = musicMetadataService.getOrFetchArtist(id);

            // Then
            assertThat(result).isEqualTo(staleArtist);
            verify(musicFetchService).refreshArtistInBackground(id);
        }
    }

    @Nested
    class GetOrFetchDiscography {

        @Test
        @DisplayName("When discography is fresh - return from DB using artistId")
        void shouldReturnFreshDiscography() {

            // Given
            String artId = "art1";
            ArtistEntity artist = createArtist(artId, LocalDateTime.now());
            artist.setDiscographyLastUpdated(LocalDateTime.now().minusDays(2));

            when(artistRepository.findById(artId)).thenReturn(Optional.of(artist));

            // When
            musicMetadataService.getOrFetchDiscography(artId);

            // Then
            verify(albumRepository).findAllByArtists_Id(artId);
            verifyNoInteractions(musicFetchService, musicCacheService);
        }

        @Test
        @DisplayName("When discography is expired - fetch from API and cache")
        void shouldFetchAndCacheExpiredDiscography() {

            // Given
            String artId = "art1";
            ArtistEntity artist = createArtist(artId, LocalDateTime.now());
            artist.setDiscographyLastUpdated(LocalDateTime.now().minusDays(20));

            List<AlbumSummaryResponse> discographyResp = List.of();
            when(artistRepository.findById(artId)).thenReturn(Optional.of(artist));
            when(musicFetchService.fetchDiscography(artId)).thenReturn(discographyResp);

            // When
            musicMetadataService.getOrFetchDiscography(artId);

            // Then
            verify(musicCacheService).cacheDiscography(artId, discographyResp);
        }

        @Test
        @DisplayName("When artist missing - should first fetch artist then discography")
        void shouldFetchArtistBeforeDiscographyIfMissing() {
            // Given
            String artId = "art1";
            ArtistResponse artResp = new ArtistResponse(artId, "Name", "url", "desc");
            ArtistEntity artEntity = createArtist(artId, LocalDateTime.now());

            when(artistRepository.findById(artId)).thenReturn(Optional.empty());
            when(musicFetchService.fetchArtist(artId)).thenReturn(artResp);
            when(musicCacheService.cacheArtist(artResp)).thenReturn(artEntity);

            when(musicFetchService.fetchDiscography(artId)).thenReturn(List.of());

            // When
            musicMetadataService.getOrFetchDiscography(artId);

            // Then
            InOrder inOrder = inOrder(musicFetchService, musicCacheService);
            inOrder.verify(musicFetchService).fetchArtist(artId);
            inOrder.verify(musicCacheService).cacheArtist(artResp);
            inOrder.verify(musicFetchService).fetchDiscography(artId);
        }

    }

    @Nested
    class GetOrFetchTracks {

        @Test
        @DisplayName("Should handle mixed track states: fresh, stale, expired, and missing")
        void shouldHandleMixedTrackStates() {
            // Given
            String freshId = "fresh";
            String staleId = "stale";
            String expiredId = "expired";
            String missingId = "missing";

            TrackEntity freshTrack = createTrackWithDate(freshId, LocalDateTime.now().minusDays(5));
            TrackEntity staleTrack = createTrackWithDate(staleId, LocalDateTime.now().minusDays(40));
            TrackEntity expiredTrack = createTrackWithDate(expiredId, LocalDateTime.now().minusDays(70));

            List<String> ids = List.of(freshId, staleId, expiredId, missingId);

            when(trackRepository.findAllById(ids))
                    .thenReturn(List.of(freshTrack, staleTrack, expiredTrack));

                // expired + missing = fetch
            TrackResponse expiredResp = new TrackResponse(expiredId, "t", null, null, "2024", List.of(), List.of());
            TrackResponse missingResp = new TrackResponse(missingId, "t", null, null, "2024", List.of(), List.of());

            TrackEntity cachedExpired = new TrackEntity();
            cachedExpired.setId(expiredId);

            TrackEntity cachedMissing = new TrackEntity();
            cachedMissing.setId(missingId);

            when(musicFetchService.fetchTrack(expiredId)).thenReturn(expiredResp);
            when(musicFetchService.fetchTrack(missingId)).thenReturn(missingResp);

            when(musicCacheService.cacheTrack(expiredResp)).thenReturn(cachedExpired);
            when(musicCacheService.cacheTrack(missingResp)).thenReturn(cachedMissing);

            // When
            Map<String, TrackEntity> result = musicMetadataService.getOrFetchTracks(ids);

            // Then

                // fresh - return
            assertThat(result.get(freshId)).isEqualTo(freshTrack);

                // stale - return but trigger async refresh
            assertThat(result.get(staleId)).isEqualTo(staleTrack);
            verify(musicFetchService).refreshTrackInBackground(staleId);

                // expired - fetch + cache
            assertThat(result.get(expiredId)).isEqualTo(cachedExpired);
            verify(musicFetchService).fetchTrack(expiredId);
            verify(musicCacheService).cacheTrack(expiredResp);

                // missing -> fetch + cache
            assertThat(result.get(missingId)).isEqualTo(cachedMissing);
            verify(musicFetchService).fetchTrack(missingId);
            verify(musicCacheService).cacheTrack(missingResp);

                // fresh should not trigger anything
            verify(musicFetchService, never()).refreshTrackInBackground(freshId);
            verify(musicFetchService, never()).fetchTrack(freshId);
        }
    }

    private TrackEntity createTrackWithDate(String id, LocalDateTime lastUpdated) {
        TrackEntity track = new TrackEntity();
        track.setId(id);
        track.setTitle("Test Track");
        track.setImageUrl(null);
        track.setReleaseDate("2020-01-01");
        track.setLastUpdated(lastUpdated);

        track.setArtists(List.of());
        track.setLinks(List.of());

        return track;
    }

    private AlbumEntity createAlbum(String id, LocalDateTime lastUpdated) {
        AlbumEntity album = new AlbumEntity();
        album.setId(id);
        album.setLastUpdated(lastUpdated);
        return album;
    }

    private ArtistEntity createArtist(String id, LocalDateTime lastUpdated) {
        ArtistEntity artist = new ArtistEntity();
        artist.setId(id);
        artist.setLastUpdated(lastUpdated);
        return artist;
    }

}