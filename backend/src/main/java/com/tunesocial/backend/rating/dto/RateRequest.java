package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.rating.model.RatingTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RateRequest(

        @NotNull
        String targetId,

        @NotNull
        RatingTargetType targetType,

        @Min(1)
        @Max(10)
        int value,

        @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters.")
        String comment
) {}