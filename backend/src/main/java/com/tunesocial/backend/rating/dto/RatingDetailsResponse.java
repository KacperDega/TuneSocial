package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.model.RateableEntity;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;

import java.time.Instant;
import java.util.stream.Collectors;

public record RatingDetailsResponse(
        Long id,
        String targetId,
        RatingTargetType targetType,
        int value,
        String comment,

        String title,
        String imageUrl,
        String authorName,

        Instant createdAt,
        Instant updatedAt
) {
    public static RatingDetailsResponse fromEntities(Rating rating, RateableEntity rateableEntity) {
        String title = rateableEntity.getTitle() == null ? "Unknown" : rateableEntity.getTitle();
        String artists = rateableEntity.getArtists().stream().map(ArtistRefDto::name).collect(Collectors.joining(", "));
        artists = artists.isEmpty() ? "Unknown" : artists;

        return new RatingDetailsResponse(
                rating.getId(),
                rating.getTargetId(),
                rating.getTargetType(),
                rating.getValue(),
                rating.getComment(),

                title,
                rateableEntity.getImageUrl(),
                artists,

                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}