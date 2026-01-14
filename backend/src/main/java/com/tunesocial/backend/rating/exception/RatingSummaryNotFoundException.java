package com.tunesocial.backend.rating.exception;

import com.tunesocial.backend.rating.model.RatingTargetType;

public class RatingSummaryNotFoundException extends RuntimeException {

    public RatingSummaryNotFoundException(String targetId, RatingTargetType type) {
        super("Rating summary not found for targetId=" + targetId + ", type=" + type);
    }
}
