package com.tunesocial.backend.music.mapper;

import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMapperTest {

    private final MetadataMapper mapper = Mappers.getMapper(MetadataMapper.class);

    @Nested
    class TrackTests {

        @Test
        @DisplayName("Should map TrackResponse to TrackEntity (ignoring album and date)")
        void shouldMapTrackResponseToTrackEntity() {
            // Given
            ArtistRefDto artist = new ArtistRefDto("654321", "Artist Name");
            AlbumRefDto albumRef = new AlbumRefDto("111111", "Album Name");

            TrackResponse response = new TrackResponse(
                    "123456",
                    "Track Title",
                    "http://image.jpg",
                    albumRef,
                    "2001-03-12",
                    List.of(artist),
                    List.of(new ExternalLinkDto(ExternalLinkType.SPOTIFY, "https://spotify.com"))
            );

            // When
            TrackEntity entity = mapper.toTrackEntity(response);

            // Then
            assertEquals("123456", entity.getId());
            assertEquals("Track Title", entity.getTitle());
            assertEquals(1, entity.getArtists().size());
            assertEquals("Artist Name", entity.getArtists().get(0).name());

            // ignored by @Mapping(ignore = true)
            assertNull(entity.getAlbum());
            assertNull(entity.getLastUpdated());
        }

        @Test
        @DisplayName("Should update existing entity with data from DTO")
        void shouldUpdateTrackEntityFromResponse() {
            // Given
            TrackEntity existingEntity = new TrackEntity();
            existingEntity.setId("123456");
            existingEntity.setTitle("Old Title");

            TrackResponse update = new TrackResponse(
                    "123456", "New Title", "http://new.jpg", null, "2024", List.of(), List.of()
            );

            // When
            mapper.updateTrackFromResponse(update, existingEntity);

            // Then
            assertEquals("New Title", existingEntity.getTitle());
            assertEquals("123456", existingEntity.getId(), "ID should remain the same.");
        }

        @Test
        @DisplayName("Should map TrackEntity to TrackResponse with AlbumRefDto conversion")
        void shouldMapTrackEntityToTrackResponseWithAlbumRef() {
            // Given
            AlbumEntity album = new AlbumEntity();
            album.setId("111111");
            album.setTitle("Album Title");

            TrackEntity entity = new TrackEntity();
            entity.setId("123456");
            entity.setTitle("Track Name");
            entity.setAlbum(album);

            // When
            TrackResponse response = mapper.toTrackResponse(entity);

            // Then
            assertEquals("123456", response.id());
            assertNotNull(response.album());
            assertEquals("111111", response.album().id());
            assertEquals("Album Title", response.album().name());

            System.out.println("Debug: " + response.toString());
        }

        @Test
        @DisplayName("Should map TrackEntity to TrackResponse when album is null")
        void shouldMapTrackEntityToTrackResponseWhenAlbumIsNull() {
            // Given
            TrackEntity entity = new TrackEntity();
            entity.setId("123456");
            entity.setTitle("Track Name");
            entity.setAlbum(null);

            // When
            TrackResponse response = mapper.toTrackResponse(entity);

            // Then
            assertNotNull(response);
            assertEquals("123456", response.id());
            assertEquals("Track Name", response.title());

            assertNull(response.album(), "AlbumRefDto should be null for tracks not belonging to any album");
        }
    }

    @Nested
    class AlbumTests {

        @Test
        @DisplayName("Should map AlbumSummaryResponse to AlbumEntity (ignoring tracks and date)")
        void shouldMapAlbumSummaryResponseToAlbumEntity() {
            // Given
            ArtistRefDto artist = new ArtistRefDto("art-1", "Artist Name");
            AlbumSummaryResponse response = new AlbumSummaryResponse(
                    "alb-1",
                    "Album Title",
                    List.of(artist),
                    "http://album-image.jpg",
                    "2023-10-01"
            );

            // When
            AlbumEntity entity = mapper.toAlbumEntity(response);

            // Then
            assertEquals("alb-1", entity.getId());
            assertEquals("Album Title", entity.getTitle());
            assertEquals("http://album-image.jpg", entity.getImageUrl());
            assertEquals(1, entity.getArtists().size());

            // Ignored fields in @Mapping
            assertTrue(entity.getTracks().isEmpty(), "Tracks should be empty/ignored");
            assertNull(entity.getLastUpdated(), "lastUpdated should be ignored");
        }

        @Test
        @DisplayName("Should update existing AlbumEntity from response (ignoring ID, tracks, date)")
        void shouldUpdateAlbumEntityFromResponse() {
            // Given
            AlbumEntity existing = new AlbumEntity();
            existing.setId("alb-1");
            existing.setTitle("Old Album Title");
            existing.setLastUpdated(LocalDateTime.now().minusDays(10));

            AlbumSummaryResponse update = new AlbumSummaryResponse(
                    "alb-1", "Updated Album Title", List.of(), "http://new-image.jpg", "2024"
            );

            // When
            mapper.updateAlbumFromResponse(update, existing);

            // Then
            assertEquals("Updated Album Title", existing.getTitle());
            assertEquals("http://new-image.jpg", existing.getImageUrl());

            // Ignored fields in @Mapping stay intact
            assertEquals("alb-1", existing.getId());
            assertNotNull(existing.getLastUpdated(), "Date should not be cleared by mapper");
        }
    }

    @Nested
    class ArtistTests {

        @Test
        @DisplayName("Should map ArtistResponse to ArtistEntity")
        void shouldMapArtistResponseToArtistEntity() {
            // Given
            ArtistResponse response = new ArtistResponse(
                    "art-1",
                    "Artist Name",
                    "http://artist-image.jpg",
                    "Artist Description"
            );

            // When
            ArtistEntity entity = mapper.toArtistEntity(response);

            // Then
            assertEquals("art-1", entity.getId());
            assertEquals("Artist Name", entity.getName());
            assertEquals("Artist Description", entity.getDescription());
            assertNull(entity.getLastUpdated());
            assertNull(entity.getDiscographyLastUpdated());
        }

        @Test
        @DisplayName("Should update ArtistEntity from response (ignoring ID, tracks and )")
        void shouldUpdateArtistEntityFromResponse() {
            // Given
            ArtistEntity existing = new ArtistEntity();
            existing.setId("art-1");
            existing.setName("Old Name");
            existing.setDiscographyLastUpdated(LocalDateTime.now().minusDays(5));

            ArtistResponse update = new ArtistResponse(
                    "art-1", "New Name", "http://new-avatar.jpg", "New Bio"
            );

            // When
            mapper.updateArtistFromResponse(update, existing);

            // Then
            assertEquals("New Name", existing.getName());
            assertEquals("New Bio", existing.getDescription());
            assertEquals("http://new-avatar.jpg", existing.getImageUrl());

            // Ignored fields in @Mapping
            assertEquals("art-1", existing.getId());
            assertNotNull(existing.getDiscographyLastUpdated(), "Discography date should not be touched by mapper");
        }
    }
}
