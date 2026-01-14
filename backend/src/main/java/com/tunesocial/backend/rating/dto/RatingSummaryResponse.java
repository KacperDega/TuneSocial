package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.rating.model.RatingSummary;
import com.tunesocial.backend.rating.model.RatingTargetType;

public record RatingSummaryResponse(
        String targetId,
        RatingTargetType targetType,
        double averageRating,
        long ratingCount
) {

    public static RatingSummaryResponse fromEntity(RatingSummary summary) {

        long count = summary.getRatingCount();

        double average = 0.0;
        if (count > 0) {
            double avg = (double) summary.getRatingSum() / count;
            average = Math.round(avg * 100.0) / 100.0;
        }

        return new RatingSummaryResponse(
                summary.getTargetId(),
                summary.getTargetType(),
                average,
                count
        );
    }
}