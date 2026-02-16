package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.mapper.MetadataMapper;
import com.tunesocial.backend.music.model.AlbumEntity;
import com.tunesocial.backend.music.model.ArtistEntity;
import com.tunesocial.backend.music.model.TrackEntity;
import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.rating.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicMetadataService metadataService;
    private final RatingService ratingService;
    private final MetadataMapper mapper;

    public TrackResponse getTrack(String trackId) {
        TrackEntity entity = metadataService.getOrFetchTrack(trackId);
        return mapper.toTrackResponse(entity);
    }

    public AlbumSummaryResponse getAlbum(String albumId) {
        AlbumEntity entity = metadataService.getOrFetchAlbum(albumId);
        return mapper.toAlbumResponse(entity);
    }

    public ArtistResponse getArtist(String artistId) {
        ArtistEntity entity = metadataService.getOrFetchArtist(artistId);
        return mapper.toArtistResponse(entity);
    }

    public List<AlbumSummaryResponse> getDiscography(String artistId) {
        List<AlbumEntity> entities = metadataService.getOrFetchDiscography(artistId);
        return mapper.toAlbumResponseList(entities);
    }

    public List<TrackResponse> getTracklist(String albumId) {
        AlbumEntity entity = metadataService.getOrFetchAlbum(albumId);
        return entity.getTracks().stream()
                .map(mapper::toTrackResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public TrackDetailsResponse getTrackDetails(String trackId, Long currentUserId) {

        TrackEntity trackEntity = metadataService.getOrFetchTrack(trackId);
        TrackResponse trackDto = mapper.toTrackResponse(trackEntity);

        RatingSummary summary =
                ratingService.getSummaryForTarget(
                        trackId,
                        RatingTargetType.TRACK
                );

        Integer userRating = ratingService.findUserRatingValue(
                currentUserId,
                trackId,
                RatingTargetType.TRACK
        );

        return TrackDetailsResponse.from(
                trackDto,
                summary.getRatingCount(),
                summary.getRatingSum(),
                userRating
        );
    }

    @Transactional(readOnly = true)
    public AlbumDetailsResponse getAlbumDetails(String albumId, Long currentUserId) {

        AlbumEntity albumEntity = metadataService.getOrFetchAlbum(albumId);

        AlbumSummaryResponse albumDto = mapper.toAlbumResponse(albumEntity);
        List<TrackResponse> trackDtos = albumEntity.getTracks().stream()
                .map(mapper::toTrackResponse)
                .toList();

        RatingSummary summary =
                ratingService.getSummaryForTarget(
                        albumId,
                        RatingTargetType.ALBUM
                );

        Integer userRating = ratingService.findUserRatingValue(
                currentUserId,
                albumId,
                RatingTargetType.ALBUM
        );

        return AlbumDetailsResponse.from(
                albumDto,
                trackDtos,
                summary.getRatingCount(),
                summary.getRatingSum(),
                userRating
        );
    }
}
