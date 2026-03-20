package com.tunesocial.backend.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @Size(min = 3, max = 32, message = "Username must be within 3-32 characters range.")
        String displayName,

        @Size(max = 500, message = "Biography can't be longer than 500 characters.")
        String bio,

        @Min(value = 1, message = "Avatar Id must be between 1 and 50")
        @Max(value = 50, message = "Avatar Id must be between 1 and 50")
        Integer avatarId, // TODO: adjust max

        LocalDate birthDate
) {}
