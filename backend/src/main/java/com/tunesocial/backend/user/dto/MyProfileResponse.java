package com.tunesocial.backend.user.dto;

import com.tunesocial.backend.user.model.enums.BirthDateVisibility;
import com.tunesocial.backend.user.model.enums.ProfileVisibility;

import java.time.Instant;
import java.time.LocalDate;

public record MyProfileResponse(
        Long userId,
        String username,
        String email,
        String displayName,
        String bio,
        Integer avatarId,
        LocalDate birthDate,
        BirthDateVisibility birthDateVisibility,
        ProfileVisibility profileVisibility,
        boolean isSetup,
        Instant updatedAt
) {}
