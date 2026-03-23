package com.tunesocial.backend.user.dto;

import java.time.Instant;

public record UserProfileResponse(
        Long userId,
        String username,
        String email,
        String bio,
        Integer avatarId,
        String birthDate
) {}
