package com.tunesocial.backend.music.dto;

import java.util.List;

public record AlbumDetailsResponse(
        AlbumSummaryResponse album,
        List<TrackResponse> tracks,

        double averageRating,
        long ratingCount,
        Integer currentUserRating
) {

    public static AlbumDetailsResponse from(
            AlbumSummaryResponse album,
            List<TrackResponse> tracks,
            long ratingCount,
            long ratingSum,
            Integer currentUserRating
    ) {

        double average = ratingCount == 0
                ? 0.0
                : (double) ratingSum / ratingCount;

        return new AlbumDetailsResponse(
                album,
                tracks,
                average,
                ratingCount,
                currentUserRating
        );
    }
}
