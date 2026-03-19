package com.tunesocial.backend.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @Size(max = 500) String bio,
        @Min(1) @Max(50) Integer avatarId, // TODO: adjust max
        LocalDate birthDate
) {}
