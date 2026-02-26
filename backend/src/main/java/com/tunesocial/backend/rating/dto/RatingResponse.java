package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.rating.model.Rating;

import java.time.Instant;

public record RatingResponse(
        Long id,
        String targetId,
        int value,
        String comment,
        Instant createdAt,
        Instant updatedAt
) {

    public static RatingResponse fromEntity(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getTargetId(),
                rating.getValue(),
                rating.getComment(),
                rating.getCreatedAt(),
                rating.getUpdatedAt()
        );
    }
}