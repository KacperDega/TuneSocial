package com.tunesocial.backend.rating.dto;

import com.tunesocial.backend.rating.model.RatingTargetType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RateRequest(

        @NotNull
        String targetId,

        @NotNull
        RatingTargetType targetType,

        @Min(1)
        @Max(10)
        int value

) {}