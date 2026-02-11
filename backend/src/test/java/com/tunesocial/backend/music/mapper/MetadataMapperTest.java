package com.tunesocial.backend.music.mapper;

import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MetadataMapperTest {

    private final MetadataMapper mapper = Mappers.getMapper(MetadataMapper.class);

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
