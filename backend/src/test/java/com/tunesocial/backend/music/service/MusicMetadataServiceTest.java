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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
            TrackEntity freshTrack = createTrackWithDaysOffset(id, -5);
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
            TrackEntity staleTrack = createTrackWithDaysOffset(id, -45);
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
            TrackEntity expiredTrack = createTrackWithDaysOffset(id, -70);
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
            AlbumEntity freshAlbum = createAlbum(id, Instant.now().minus(5, ChronoUnit.DAYS));
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
            ArtistEntity staleArtist = createArtist(id, Instant.now().minus(45, ChronoUnit.DAYS));
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
            ArtistEntity artist = createArtist(artId, Instant.now());
            artist.setDiscographyLastUpdated(Instant.now().minus(2, ChronoUnit.DAYS));

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
            ArtistEntity artist = createArtist(artId, Instant.now());
            artist.setDiscographyLastUpdated(Instant.now().minus(20, ChronoUnit.DAYS));

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
            ArtistEntity artEntity = createArtist(artId, Instant.now());

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

            TrackEntity freshTrack = createTrackWithDaysOffset(freshId, -5);
            TrackEntity staleTrack = createTrackWithDaysOffset(staleId, -40);
            TrackEntity expiredTrack = createTrackWithDaysOffset(expiredId, -70);

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

                // fresh - just return
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

    @Nested
    class GetOrFetchAlbums {

        @Test
        @DisplayName("Should handle mixed album states: fresh, stale, expired, and missing")
        void shouldHandleMixedAlbumStates() {
            // Given
            String freshId = "fresh";
            String staleId = "stale";
            String expiredId = "expired";
            String missingId = "missing";

            AlbumEntity freshAlbum = createAlbum(freshId, Instant.now().minus(5, ChronoUnit.DAYS));
            AlbumEntity staleAlbum = createAlbum(staleId, Instant.now().minus(40, ChronoUnit.DAYS));
            AlbumEntity expiredAlbum = createAlbum(expiredId, Instant.now().minus(70, ChronoUnit.DAYS));

            List<String> ids = List.of(freshId, staleId, expiredId, missingId);

            when(albumRepository.findAllById(ids))
                    .thenReturn(List.of(freshAlbum, staleAlbum, expiredAlbum));

                // expired + missing = fetch
            AlbumSummaryResponse expiredResp =
                    new AlbumSummaryResponse(expiredId, "exp", null, null, null);

            AlbumSummaryResponse missingResp =
                    new AlbumSummaryResponse(missingId, "miss", null, null, null);

            List<TrackResponse> expiredTracks = List.of();
            List<TrackResponse> missingTracks = List.of();

            AlbumEntity cachedExpired = new AlbumEntity();
            cachedExpired.setId(expiredId);

            AlbumEntity cachedMissing = new AlbumEntity();
            cachedMissing.setId(missingId);

            when(musicFetchService.fetchAlbum(expiredId)).thenReturn(expiredResp);
            when(musicFetchService.fetchAlbum(missingId)).thenReturn(missingResp);

            when(musicFetchService.fetchTracklist(expiredId)).thenReturn(expiredTracks);
            when(musicFetchService.fetchTracklist(missingId)).thenReturn(missingTracks);

            when(musicCacheService.cacheAlbumWithTracks(expiredResp, expiredTracks))
                    .thenReturn(cachedExpired);

            when(musicCacheService.cacheAlbumWithTracks(missingResp, missingTracks))
                    .thenReturn(cachedMissing);

            // When
            Map<String, AlbumEntity> result = musicMetadataService.getOrFetchAlbums(ids);

            // Then

                // fresh - just return
            assertThat(result.get(freshId)).isEqualTo(freshAlbum);

                // stale - return but trigger async refresh
            assertThat(result.get(staleId)).isEqualTo(staleAlbum);
            verify(musicFetchService).refreshAlbumInBackground(staleId);

                // expired - fetch + cache
            assertThat(result.get(expiredId)).isEqualTo(cachedExpired);
            verify(musicFetchService).fetchAlbum(expiredId);
            verify(musicFetchService).fetchTracklist(expiredId);
            verify(musicCacheService).cacheAlbumWithTracks(expiredResp, expiredTracks);

                // missing -> fetch + cache
            assertThat(result.get(missingId)).isEqualTo(cachedMissing);
            verify(musicFetchService).fetchAlbum(missingId);
            verify(musicFetchService).fetchTracklist(missingId);
            verify(musicCacheService).cacheAlbumWithTracks(missingResp, missingTracks);

                // fresh should not trigger anything
            verify(musicFetchService, never()).refreshAlbumInBackground(freshId);
            verify(musicFetchService, never()).fetchAlbum(freshId);
            verify(musicFetchService, never()).fetchTracklist(freshId);
        }
    }

    private TrackEntity createTrackWithDaysOffset(String id, int daysOffset) {
        TrackEntity track = new TrackEntity();
        track.setId(id);
        track.setTitle("Test Track");
        track.setImageUrl(null);
        track.setReleaseDate("2020-01-01");

        track.setLastUpdated(Instant.now().plus(daysOffset, ChronoUnit.DAYS));

        track.setArtists(List.of());
        track.setLinks(List.of());

        return track;
    }

    private AlbumEntity createAlbum(String id, Instant lastUpdated) {
        AlbumEntity album = new AlbumEntity();
        album.setId(id);
        album.setLastUpdated(lastUpdated);
        return album;
    }

    private ArtistEntity createArtist(String id, Instant lastUpdated) {
        ArtistEntity artist = new ArtistEntity();
        artist.setId(id);
        artist.setLastUpdated(lastUpdated);
        return artist;
    }

}