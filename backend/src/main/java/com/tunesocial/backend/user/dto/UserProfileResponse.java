package com.tunesocial.backend.user.dto;

import java.time.Instant;

public record UserProfileResponse(
        Long userId,
        String username,
        String displayName,
        String bio,
        Integer avatarId,
        String formattedBirthDate,
        boolean isSetup,
        Instant updatedAt
) {}
