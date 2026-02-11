package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.repository.TrackRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicMetadataServiceTest {

    @Mock private TrackRepository trackRepository;
    @Mock private MusicFetchService musicFetchService;
    @Mock private MusicCacheService musicCacheService;

    @InjectMocks private MusicMetadataService musicMetadataService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(musicMetadataService, "CACHE_TTL_DAYS", 30);
        ReflectionTestUtils.setField(musicMetadataService, "CACHE_EXPIRED_DAYS", 60);
    }

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

    private TrackEntity createTrackWithDate(String id, LocalDateTime date) {
        TrackEntity track = new TrackEntity();
        track.setId(id);
        track.setTitle("Test Track");
        track.setImageUrl(null);
        track.setReleaseDate("2020-01-01");
        track.setLastUpdated(date);

        track.setArtists(List.of());
        track.setLinks(List.of());

        return track;
    }
}