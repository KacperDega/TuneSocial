package com.tunesocial.backend.music.mapper;

import com.tunesocial.backend.music.dto.ArtistResponse;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.dto.AlbumSummaryResponse;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.music.dto.TrackResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MusicEntityMapper {

    //TRACK

    // ignore album on purpose, handle it in service
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    TrackEntity toTrackEntity(TrackResponse response);

    @Mapping(target = "album.id", source = "entity.album.id")
    @Mapping(target = "album.name", source = "entity.album.title")
    TrackResponse toTrackResponse(TrackEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "album", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    void updateTrackFromResponse(TrackResponse response, @MappingTarget TrackEntity entity);


    // ALBUM
    @Mapping(target = "tracks", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    AlbumEntity toAlbumEntity(AlbumSummaryResponse response);

    AlbumSummaryResponse toAlbumResponse(AlbumEntity entity);

    List<AlbumSummaryResponse> toAlbumResponseList(List<AlbumEntity> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tracks", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    void updateAlbumFromResponse(AlbumSummaryResponse response, @MappingTarget AlbumEntity entity);


    // ARTIST
    ArtistEntity toArtistEntity(ArtistResponse response);

    ArtistResponse toArtistResponse(ArtistEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdated", ignore = true)
    @Mapping(target = "discographyLastUpdated", ignore = true)
    void updateArtistFromResponse(ArtistResponse response, @MappingTarget ArtistEntity entity);
}
