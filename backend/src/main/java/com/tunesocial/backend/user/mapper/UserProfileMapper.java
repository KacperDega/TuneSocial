package com.tunesocial.backend.user.mapper;

import com.tunesocial.backend.user.dto.MyProfileResponse;
import com.tunesocial.backend.user.dto.UserProfileResponse;
import com.tunesocial.backend.user.model.UserProfile;
import com.tunesocial.backend.user.model.enums.BirthDateVisibility;
import com.tunesocial.backend.user.model.enums.ProfileVisibility;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserProfileMapper {

    public MyProfileResponse toMyResponse(UserProfile profile) {
        return new MyProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getUser().getEmail(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarId(),
                profile.getBirthDate(),
                profile.getBirthDateVisibility(),
                profile.getProfileVisibility(),
                profile.isSetup(),
                profile.getUpdatedAt()
        );
    }

    public UserProfileResponse toPublicResponse(UserProfile profile, Long currentUserId) {
        boolean isAuthenticated = currentUserId != null;
        ProfileVisibility visibility = profile.getProfileVisibility();

        if (!profile.isSetup()) {
            return createNotSetupResponse(profile);
        }

        if (visibility == ProfileVisibility.PRIVATE ||
                (visibility == ProfileVisibility.REGISTERED_ONLY && !isAuthenticated)) {
            return createRestrictedResponse(profile);
        }

        String formattedBirthDate = formatPublicBirthDate(
                profile.getBirthDate(),
                profile.getBirthDateVisibility()
        );

        return new UserProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getAvatarId(),
                formattedBirthDate,
                profile.isSetup(),
                profile.getUpdatedAt()
        );
    }


    private UserProfileResponse createRestrictedResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getDisplayName(),
                null,
                profile.getAvatarId(),
                null,
                profile.isSetup(),
                profile.getUpdatedAt()
        );
    }

    private UserProfileResponse createNotSetupResponse(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUser().getUsername(),
                profile.getDisplayName(),
                null,
                profile.getAvatarId(),
                null,
                false,
                profile.getUpdatedAt()
        );
    }

    private String formatPublicBirthDate(LocalDate date, BirthDateVisibility visibility) {
        if (date == null || visibility == BirthDateVisibility.HIDDEN) {
            return null;
        }

        return switch (visibility) {
            case FULL -> date.toString();
            case YEAR_ONLY -> String.valueOf(date.getYear());
            case MONTH_AND_DAY -> String.format("%02d-%02d", date.getDayOfMonth(), date.getMonthValue());
            case HIDDEN -> null;
        };
    }
}