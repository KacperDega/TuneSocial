package com.tunesocial.backend.music.dto;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;

import java.util.List;

public record TrackDetailsResponse(
        String id,
        String title,
        String imageUrl,
        @Embedded AlbumRefDto album,
        String releaseDate,
        @ElementCollection List<ArtistRefDto> artists,
        @ElementCollection List<ExternalLinkDto> links,

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
