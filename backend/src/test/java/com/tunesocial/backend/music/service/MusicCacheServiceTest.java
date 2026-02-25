package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.AlbumRefDto;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.dto.TrackResponse;
import com.tunesocial.backend.music.exception.MusicItemNotFoundException;
import com.tunesocial.backend.music.mapper.MusicEntityMapper;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MusicCacheServiceTest {

    @Mock private TrackRepository trackRepository;
    @Mock private AlbumRepository albumRepository;
    @Mock private MusicEntityMapper musicEntityMapper;
    @Mock private ArtistRepository artistRepository;

    @InjectMocks private MusicCacheService musicCacheService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(musicCacheService, "CACHE_TTL_DAYS", 30);
    }

    @Test
    @DisplayName("cacheTrack should create album stub if track's album is not in cache")
    void cacheTrack_ShouldCreateAlbumStub_WhenAlbumMissing() {
        // Given
        String albumId = "alb-01";
        AlbumRefDto albumRef = new AlbumRefDto(albumId, "Album Stub");
        TrackResponse trackResp = new TrackResponse("tr-1", "Song", "url", albumRef, "2024", List.of(), List.of());

        when(musicEntityMapper.toTrackEntity(trackResp)).thenReturn(new TrackEntity());
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());
        when(albumRepository.save(any(AlbumEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(trackRepository.save(any(TrackEntity.class))).thenAnswer(i -> i.getArgument(0));

        // When
        TrackEntity result = musicCacheService.cacheTrack(trackResp);

        // Then
        verify(albumRepository).save(argThat(album ->
                album.getId().equals(albumId) &&
                        album.getTitle().equals("Album Stub") &&
                        album.getLastUpdated().isBefore(LocalDateTime.now().minusDays(30))
        ));
        assertThat(result.getAlbum()).isNotNull();
    }

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
        when(musicEntityMapper.toTrackEntity(any(TrackResponse.class))).thenReturn(new TrackEntity());

        // When
        musicCacheService.cacheAlbumWithTracks(albumResp, List.of(trackResp));

        // Then
        verify(musicEntityMapper).updateAlbumFromResponse(eq(albumResp), eq(existingAlbum));
        verify(albumRepository).save(existingAlbum);

        assertThat(existingAlbum.getLastUpdated()).isAfter(LocalDateTime.now().minusSeconds(5));
    }

    @Nested
    class cacheDiscography {

        @Test
        @DisplayName("should throw exception when artist is missing in DB")
        void cacheDiscography_ShouldThrowException_WhenArtistNotFound() {
            // Given
            String artistId = "art-123";
            when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> musicCacheService.cacheDiscography(artistId, List.of()))
                    .isInstanceOf(MusicItemNotFoundException.class)
                    .hasMessageContaining("Artist not found");

            verifyNoInteractions(albumRepository, musicEntityMapper);
        }

        @Test
        @DisplayName("should update existing albums and create stubs for new ones")
        void cacheDiscography_ShouldMergeExistingAndNewAlbums() {
            // Given
            String artistId = "art-1";
            ArtistEntity artist = new ArtistEntity();
            artist.setId(artistId);

            AlbumSummaryResponse existingAlbumResp = new AlbumSummaryResponse("alb-exists", "New Title", List.of(), "url", "2024");
            AlbumSummaryResponse newAlbumResp = new AlbumSummaryResponse("alb-new", "Fresh Album", List.of(), "url", "2024");
            List<AlbumSummaryResponse> discography = List.of(existingAlbumResp, newAlbumResp);

            AlbumEntity existingAlbumEntity = new AlbumEntity();
            existingAlbumEntity.setId("alb-exists");
            existingAlbumEntity.setTitle("Old Title");
            existingAlbumEntity.setLastUpdated(LocalDateTime.now().minusDays(5));

            when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));

                // existing album
            when(albumRepository.findById("alb-exists")).thenReturn(Optional.of(existingAlbumEntity));

                // new album
            when(albumRepository.findById("alb-new")).thenReturn(Optional.empty());
            when(musicEntityMapper.toAlbumEntity(newAlbumResp)).thenReturn(new AlbumEntity());

            when(albumRepository.save(any(AlbumEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            List<AlbumEntity> result = musicCacheService.cacheDiscography(artistId, discography);

            // Then
            assertThat(result).hasSize(2);
            assertThat(artist.getDiscographyLastUpdated()).isAfter(LocalDateTime.now().minusSeconds(5));

                // verify existing
            verify(musicEntityMapper).updateAlbumFromResponse(eq(existingAlbumResp), eq(existingAlbumEntity));
            assertThat(existingAlbumEntity.getLastUpdated()).isBefore(LocalDateTime.now().minusSeconds(1));

                // verify new
            verify(musicEntityMapper).toAlbumEntity(newAlbumResp);
            AlbumEntity stub = result.stream().filter(a -> "alb-new".equals(a.getId()) || a.getId() == null).findFirst().get();
            assertThat(stub.getLastUpdated()).isBefore(LocalDateTime.now().minusDays(30));
        }
    }
}
