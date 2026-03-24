package com.tunesocial.backend.user.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record SetupProfileRequest(
        @NotBlank(message = "Display name is required to complete setup.")
        @Size(min = 3, max = 32, message = "Display name must be within 3-32 characters range.")
        String displayName,

        @NotBlank(message = "Biography is required to complete setup.")
        @Size(max = 500, message = "Biography can't be longer than 500 characters.")
        String bio,

        @NotNull(message = "Avatar selection is required to complete setup.")
        @Min(value = 1, message = "Avatar Id must be between 1 and 50")
        @Max(value = 50, message = "Avatar Id must be between 1 and 50")
        Integer avatarId, // TODO: adjust max

        @NotNull(message = "Birth date is required to complete setup.")
        LocalDate birthDate
) {}
