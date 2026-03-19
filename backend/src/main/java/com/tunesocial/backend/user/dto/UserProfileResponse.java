package com.tunesocial.backend.user.dto;

import com.tunesocial.backend.user.model.UserProfile;

import java.time.Instant;
import java.time.LocalDate;

public record UserProfileResponse(
        Long userId,
        String username,
        String email,
        String bio,
        Integer avatarId,
        LocalDate birthDate,
        Instant updatedAt
) {
    public static UserProfileResponse fromEntity(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getBio(),
                profile.getAvatarId(),
                profile.getBirthDate(),
                profile.getUpdatedAt()
        );
    }
}
