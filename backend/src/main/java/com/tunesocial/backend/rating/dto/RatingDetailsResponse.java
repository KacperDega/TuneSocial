package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.music.dto.ArtistRefDto;
import com.tunesocial.backend.music.model.RateableEntity;
import com.tunesocial.backend.rating.model.Rating;
import com.tunesocial.backend.rating.model.RatingTargetType;
import com.tunesocial.backend.user.dto.UserRefDto;

import java.time.Instant;
import java.util.stream.Collectors;

public record RatingDetailsResponse(
        Long id,
        String targetId,
        RatingTargetType targetType,
        int value,
        String comment,

        UserRefDto author,

        String title,
        String imageUrl,
        String authorName,

        Instant createdAt,
        Instant updatedAt
) {
    public static RatingDetailsResponse fromEntities(Rating rating, RateableEntity rateableEntity, UserRefDto author) {
        String title = rateableEntity.getTitle() == null ? "Unknown" : rateableEntity.getTitle();
        String artists = rateableEntity.getArtists().stream().map(ArtistRefDto::name).collect(Collectors.joining(", "));
        artists = artists.isEmpty() ? "Unknown" : artists;

        UserRefDto finalAuthor = author != null ? author : new UserRefDto(
                rating.getUserId(),
                null,
                "User_" + rating.getUserId(),
                1
        );

        return new RatingDetailsResponse(
                rating.getId(),
                rating.getTargetId(),
                rating.getTargetType(),
                rating.getRatingValue(),
                rating.getComment(),

                finalAuthor,

                title,
                rateableEntity.getImageUrl(),
                artists,

                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}