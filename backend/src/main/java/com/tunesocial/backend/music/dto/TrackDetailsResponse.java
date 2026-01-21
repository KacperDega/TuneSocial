package com.tunesocial.backend.music.dto;

import java.util.List;

public record TrackDetailsResponse(
        String id,
        String title,
        String imageUrl,
        AlbumRefDto album,
        String releaseDate,
        List<ArtistRefDto> artists,
        List<ExternalLinkDto> links,

        double averageRating,
        long ratingCount,
        Integer currentUserRating
) {

    public static TrackDetailsResponse from(
            TrackResponse track,
            long ratingCount,
            long ratingSum,
            Integer currentUserRating
    ) {

        double average = ratingCount == 0
                ? 0.0
                : (double) ratingSum / ratingCount;

        return new TrackDetailsResponse(
                track.id(),
                track.title(),
                track.imageUrl(),
                track.album(),
                track.releaseDate(),
                track.artists(),
                track.links(),
                average,
                ratingCount,
                currentUserRating
        );
    }
}
