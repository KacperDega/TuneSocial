package com.tunesocial.backend.user.mapper;

import com.tunesocial.backend.user.dto.UserProfileResponse;
import com.tunesocial.backend.user.model.UserProfile;
import com.tunesocial.backend.user.model.enums.BirthDateVisibility;
import com.tunesocial.backend.user.model.enums.ProfileVisibility;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserProfileMapper {

    public UserProfileResponse toResponse(UserProfile profile, Long currentUserId) {
        Long ownerId = profile.getId();
        boolean isOwner = currentUserId != null && currentUserId.equals(ownerId);
        boolean isAuthenticated = currentUserId != null;

        ProfileVisibility visibility = profile.getProfileVisibility();

        // private / not authenticated
        if (!isOwner) {
            if (visibility == ProfileVisibility.PRIVATE) {
                return createRestrictedResponse(profile);
            }

            if (visibility == ProfileVisibility.REGISTERED_ONLY && !isAuthenticated) {
                return createRestrictedResponse(profile);
            }
        }

        // public / owner / registered
        String formattedBirthDate = formatBirthDate(
                profile.getBirthDate(),
                profile.getBirthDateVisibility(),
                isOwner
        );

        return new UserProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                isOwner ? profile.getUser().getEmail() : null, // owner only
                profile.getBio(),
                profile.getAvatarId(),
                formattedBirthDate
        );
    }


    private UserProfileResponse createRestrictedResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                null,
                "This profile is private.",
                profile.getAvatarId(),
                null
        );
    }

    private String formatBirthDate(LocalDate date, BirthDateVisibility visibility, boolean isOwner) {
        if (date == null || (visibility == BirthDateVisibility.HIDDEN && !isOwner)) {
            return null;
        }

        if (isOwner) {
            return date.toString();
        }

        return switch (visibility) {
            case FULL -> date.toString();
            case YEAR_ONLY -> String.valueOf(date.getYear());
            case MONTH_AND_DAY -> String.format("%02d-%02d", date.getDayOfMonth(), date.getMonthValue());
            case HIDDEN -> null;
        };
    }
}