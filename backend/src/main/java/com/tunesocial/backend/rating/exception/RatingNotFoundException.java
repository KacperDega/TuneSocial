package com.tunesocial.backend.rating.exception;

import com.tunesocial.backend.rating.model.RatingTargetType;

public class RatingNotFoundException extends RuntimeException {

    public RatingNotFoundException(Long ratingId) {
        super("Rating not found with id: " + ratingId);
    }

    public RatingNotFoundException(Long userId, String targetId, RatingTargetType targetType) {
        super("Rating not found for user: " + userId + " and target: {" + targetId + ", " + targetType + "}");
    }
}