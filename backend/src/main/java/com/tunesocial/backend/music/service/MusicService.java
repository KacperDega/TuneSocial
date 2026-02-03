package com.tunesocial.backend.music.service;

import com.tunesocial.backend.music.dto.*;
import com.tunesocial.backend.music.provider.MusicDataProvider;
import com.tunesocial.backend.rating.model.Rating;
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

    private final MusicDataProvider provider;
    private final RatingService ratingService;

    public TrackResponse getTrack(String trackId) {
        return provider.getTrack(trackId);
    }

    public AlbumSummaryResponse getAlbum(String albumId) {
        return provider.getAlbum(albumId);
    }

    public ArtistResponse getArtist(String artistId) {
        return provider.getArtist(artistId);
    }

    public List<AlbumSummaryResponse> getDiscography(String artistId) {
        return provider.getDiscography(artistId);
    }

    public List<TrackResponse> getTracklist(String albumId) {
        return provider.getTrackList(albumId);
    }

    @Transactional(readOnly = true)
    public TrackDetailsResponse getTrackDetails(String trackId, Long currentUserId) {

        TrackResponse track = provider.getTrack(trackId);

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
                track,
                summary.getRatingCount(),
                summary.getRatingSum(),
                userRating
        );
    }

    @Transactional(readOnly = true)
    public AlbumDetailsResponse getAlbumDetails(String albumId, Long currentUserId) {

        AlbumSummaryResponse album = provider.getAlbum(albumId);
        List<TrackResponse> tracks = provider.getTrackList(albumId);

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
                album,
                tracks,
                summary.getRatingCount(),
                summary.getRatingSum(),
                userRating
        );
    }
}
