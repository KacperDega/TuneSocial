package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.mapper.MetadataMapper;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.repository.AlbumRepository;
import com.tunesocial.backend.music.repository.TrackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicCacheServiceTest {

    @Mock private TrackRepository trackRepository;
    @Mock private AlbumRepository albumRepository;
    @Mock private MetadataMapper metadataMapper;

    @InjectMocks private MusicCacheService musicCacheService;

    @Test
    @DisplayName("CacheAlbumWithTracks should update exisitng album and add new tracks")
    void shouldUpdateAlbumAndMergeTracks() {
        // Given
        String albumId = "a1";
        AlbumSummaryResponse albumResp = new AlbumSummaryResponse(albumId, "New Title", null, null, null);
        TrackResponse trackResp = new TrackResponse("t1", "Track", null, null, null, null, null);

        AlbumEntity existingAlbum = new AlbumEntity();
        existingAlbum.setId(albumId);
        existingAlbum.setTitle("Old Title");

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existingAlbum));
        when(trackRepository.findById("t1")).thenReturn(Optional.empty());
        when(metadataMapper.toEntity(any(TrackResponse.class))).thenReturn(new TrackEntity());

        // When
        musicCacheService.cacheAlbumWithTracks(albumResp, List.of(trackResp));

        // Then
        verify(metadataMapper).updateAlbumFromResponse(eq(albumResp), eq(existingAlbum));
        verify(albumRepository).save(existingAlbum);

        assertThat(existingAlbum.getLastUpdated()).isAfter(LocalDateTime.now().minusSeconds(5));
    }
}
