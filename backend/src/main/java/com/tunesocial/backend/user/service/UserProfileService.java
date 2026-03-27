package com.tunesocial.backend.user.service;

import com.tunesocial.backend.user.dto.MyProfileResponse;
import com.tunesocial.backend.user.dto.SetupProfileRequest;
import com.tunesocial.backend.user.dto.UpdateProfileRequest;
import com.tunesocial.backend.user.dto.UserProfileResponse;
import com.tunesocial.backend.user.exception.ProfileAlreadySetupException;
import com.tunesocial.backend.user.exception.UserNotFoundException;
import com.tunesocial.backend.user.exception.UserProfileNotFoundException;
import com.tunesocial.backend.user.mapper.UserMapper;
import com.tunesocial.backend.user.mapper.UserProfileMapper;
import com.tunesocial.backend.user.model.User;
import com.tunesocial.backend.user.model.UserProfile;
import com.tunesocial.backend.user.repository.UserProfileRepository;
import com.tunesocial.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByUsername(String username, Long currentUserId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        UserProfile profile = profileRepository.findById(user.getId())
                .orElseThrow(() -> new UserProfileNotFoundException(user.getId()));

        return userProfileMapper.toPublicResponse(profile, currentUserId);
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return userProfileMapper.toMyResponse(profile);
    }

    @Transactional
    public MyProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new UserProfileNotFoundException(userId));

        userMapper.updateProfileFromDto(request, profile);

        if (profile.getDisplayName() != null) {
            profile.setDisplayName(profile.getDisplayName().trim());
        }

        UserProfile updatedProfile = profileRepository.save(profile);
        log.info("User profile updated successfully for userId: {}", userId);

        return userProfileMapper.toMyResponse(updatedProfile);
    }

    @Transactional
    public MyProfileResponse setupProfile(Long userId, SetupProfileRequest request) {
        UserProfile profile = profileRepository.findById(userId)
                .orElseThrow(() -> new UserProfileNotFoundException(userId));

        if (profile.isSetup()) {
            throw new ProfileAlreadySetupException(userId);
        }

        profile.setDisplayName(request.displayName().trim());
        profile.setBio(request.bio().trim());
        profile.setAvatarId(request.avatarId());
        profile.setBirthDate(request.birthDate());

        profile.setSetup(true);

        UserProfile savedProfile = profileRepository.save(profile);
        log.info("User profile setup completed for userId: {}", userId);

        return userProfileMapper.toMyResponse(savedProfile);
    }
}
