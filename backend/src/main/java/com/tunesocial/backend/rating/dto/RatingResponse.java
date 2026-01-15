package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.rating.model.Rating;

import java.time.Instant;

public record RatingResponse(
        Long id,
        String targetId,
        int value,
        Instant createdAt
) {

    public static RatingResponse fromEntity(Rating rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getTargetId(),
                rating.getValue(),
                rating.getCreatedAt()
        );
    }
}